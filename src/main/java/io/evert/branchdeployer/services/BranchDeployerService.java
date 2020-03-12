package io.evert.branchdeployer.services;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import io.evert.branchdeployer.config.BranchDeployerConfig;
import io.evert.branchdeployer.config.model.Project;
import io.evert.branchdeployer.webhook.WebhookModel;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BranchDeployerService {

    @Autowired
    GitService gitService;

    @Autowired
    BranchDeployerConfig config;

    @Autowired
    DockerService dockerService;

    @Autowired
    DigitalOceanService digitalOceanService;

    Boolean deleteDirectory(File directoryToBeDeleted) {
        // https://www.baeldung.com/java-delete-directory
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    public String validatePath(String rootCloneDir, String pathWithNamespace) {
        File cloneDir = new File(rootCloneDir + pathWithNamespace);

        String canonicalPath = null;
        try {
            canonicalPath = cloneDir.getCanonicalPath();
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error(e.getStackTrace().toString());
        }

        if (canonicalPath == null) {
            log.error(String.format("Unable to get canonical path from %s", cloneDir));
            return null;
        }

        if (!canonicalPath.equals(cloneDir.getPath())) {
            log.error(String.format("Canonical path [%s] does not match clone dir [%s]", canonicalPath, cloneDir));
            return null;
        }

        if (!canonicalPath.startsWith(rootCloneDir)) {
            log.error(String.format("Canonical path is outside the allowed directory: %s", canonicalPath));
            return null;
        }

        return canonicalPath;
    }

    public FileOutputStream getStream(String canonicalBranchLockFile) {

        File branchLockFile = new File(canonicalBranchLockFile);
        try {
            branchLockFile.createNewFile();
        } catch (IOException e) {
            log.error("Failed to create lock file", e);
            return null;
        }

        FileOutputStream fos;
        try {
            fos = new FileOutputStream(branchLockFile);
        } catch (FileNotFoundException e) {
            log.error(String.format("Failed to create lock file stream: %s", branchLockFile.getPath()), e);
            return null;
        }

        return fos;
    }

    public FileLock getLock(FileOutputStream fos) {
        // https://www.linuxtopia.org/online_books/programming_books/thinking_in_java/TIJ314_030.htm

        FileLock fl;
        FileChannel ch = fos.getChannel();
        try {
            fl = ch.lock();
        } catch (IOException e) {
            log.error("Failed to acquire lock", e);
            return null;
        }

        if(fl != null) {
            log.info("Acquired file lock");
            return fl;
        }

        return null;
    }

    public boolean releaseLock(FileOutputStream fos, FileLock lock) {

        log.info("Releasing lock...");
        try {
            lock.release();
        } catch (IOException e) {
            log.error("Failed to release lock", e);
            return false;
        }
    
        try {
            fos.close();
        } catch (IOException e2) {
            log.error("Failed to close stream", e2);
            return false;
        }

        log.info("Lock released.");
        return true;
    }

    @Async
    public void asyncDeployBranch(WebhookModel webhook, Project project) {

        String canonicalPath = validatePath(config.getRootCloneDirectory(), webhook.getPathWithNamepace() + "_" + webhook.getBranchName());

        if (canonicalPath == null) {
            return;
        }

        String canonicalBranchLockFile = validatePath(config.getRootCloneDirectory(), webhook.getPathWithNamepace() + "_" + webhook.getBranchName() + ".lock");
        if (canonicalBranchLockFile == null) {
            log.error(String.format("Could not validate lock file path: %s", canonicalBranchLockFile));
            return;
        }
        final File lockFile = new File(canonicalBranchLockFile);
    
        // intern is not the best, but good enough for now.
        synchronized(lockFile.toPath().toString().intern()) {
            log.info(String.format("Locking %s", canonicalBranchLockFile));
            FileOutputStream fos = getStream(canonicalBranchLockFile);
            if (fos == null) {
                log.error("Could not get File Output Stream");
                return;
            }
            FileLock lock = getLock(fos);

            if (lock == null) {
                log.error("Could not get lock");
                try {
                    fos.close();
                } catch (Exception e) {
                    log.error("Failed to close file stream", e);
                }
                return;
            }
            
            // Warning: Potential race condition. May deploy an out of date commit.
            try {
                Map<String, String> env = new HashMap<String, String>();
                // Warning: branch name is not validated
                env.put("TAG_NAME",webhook.getCommitHash());
                File repoDir = new File(canonicalPath);
                if (repoDir.exists()) {
                    dockerService.stackDown(repoDir, env);
                }

                log.warn(String.format("Deleting directory [%s]", canonicalPath));
                deleteDirectory(new File(canonicalPath));
            
                Boolean success = gitService.cloneRepo(
                    webhook.getUri(), 
                    webhook.getBranchName(),
                    canonicalPath,
                    project.getUsername(), 
                    project.getPassword());

                if (!success) {
                    log.error("Failed to clone repo");
                    return;
                }

                List<String> localFilesToCopy = config.getSecretToProjectMap().get(webhook.getWebhookSecret()).getInsertLocalFiles();
                for (String localFile : localFilesToCopy) {
                    File src = new File(localFile);
                    File dst = new File(canonicalPath + "/" + localFile);
                    log.info(String.format("Copying %s -> %s", src, dst));

                    try {
                        FileUtils.copyFile(src, dst);
                    } catch (IOException e) {
                        log.error("Failed to copy file", e);
                        return;
                    }
                }

                Boolean dockerSucc = dockerService.stackUp(repoDir, env);

                if (!dockerSucc) {
                    log.error("Failed to start docker containers");
                    return;
                }

                log.info("Creating domain...");
                Boolean domainSucc = digitalOceanService.createSubDomain(
                    webhook.getBranchName(),
                    config.getSecretToProjectMap().get(webhook.getWebhookSecret()).getName(),
                    config.getDomain());

                if (!domainSucc) {
                    log.error("Failed to create domain");
                    return;
                }
            
            } finally {
                log.info("Freeing lock...");
                releaseLock(fos, lock);
            }
        }
    }
}

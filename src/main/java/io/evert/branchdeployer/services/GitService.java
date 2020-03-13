package io.evert.branchdeployer.services;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class GitService {

    public Boolean cloneRepo(String uri, String branch, String directory, String username, String password) {
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(uri);
        cloneCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(username, password));
        cloneCommand.setDirectory(new File(directory));
        cloneCommand.setBranchesToClone(Arrays.asList("refs/heads/" + branch));
        cloneCommand.setBranch("refs/heads/" + branch);
        log.info(String.format("Cloning %s to %s", uri, directory));

        try {
            cloneCommand.call();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
        
        log.info("Done Cloning");
        return true;
    }
}

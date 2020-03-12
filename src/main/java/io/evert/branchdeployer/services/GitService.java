package io.evert.branchdeployer.services;

import org.springframework.stereotype.Service;

@Service
public class GitService {

    public void cloneRepo(String username, String password, String url, String branch) {
        // Git git = Git.cloneRepository()
        // .setURI("https://github.com/eclipse/jgit.git")
        // .setDirectory("/path/to/repo")
        // .call();
    }
}

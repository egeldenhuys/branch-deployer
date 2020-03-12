package io.evert.branchdeployer.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import io.evert.branchdeployer.config.model.Project;

@Slf4j

@ConstructorBinding
@ConfigurationProperties("branch-deployer")
public class BranchDeployerConfig {

    @Getter private String rootCloneDirectory;
    @Getter private List<Project> projects = new ArrayList<>();
    @Getter private Map<String, Project> secretToProjectMap = new HashMap<>();
    @Getter private String digitalOceanAuthToken;
    @Getter private String cnameValue;
    @Getter private String domain;

    public BranchDeployerConfig(String digitalOceanAuthToken, String cnameValue, String rootCloneDirectory, List<Project> projects, String domain) {
        log.warn(digitalOceanAuthToken);
        this.projects = projects;
        this.digitalOceanAuthToken = digitalOceanAuthToken;
        this.domain = domain;
        this.cnameValue = cnameValue;
        if (!rootCloneDirectory.endsWith("/")) {
            rootCloneDirectory += "/";
        }
        this.rootCloneDirectory = rootCloneDirectory;
        log.info(rootCloneDirectory);

        for (Project project : this.projects) {
            this.secretToProjectMap.put(project.getWebhookSecret(), project);
            log.info(String.format("%s -> %s", project.getWebhookSecret(), project.getName()));
        }
    }
}

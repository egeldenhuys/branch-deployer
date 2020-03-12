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
public class Config {

    @Getter private List<Project> projects = new ArrayList<>();
    @Getter private Map<String, Project> secretToProjectMap = new HashMap<>();

    public Config(List<Project> projects) {
        log.warn("Creating config instance using params");
        log.warn(projects.toString());
        this.projects = projects;
        this.init();
    }

    public void init() {
        log.warn("Initialising config");
        log.warn(this.projects.toString());

        for (Project project : this.projects) {
            this.secretToProjectMap.put(project.getWebhookSecret(), project);
            log.info(String.format("%s -> %s", project.getWebhookSecret(), project.getName()));
        }
    }
}

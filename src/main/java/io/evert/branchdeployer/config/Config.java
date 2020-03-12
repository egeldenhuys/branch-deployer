package io.evert.branchdeployer.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Getter;
import lombok.Setter;

import io.evert.branchdeployer.config.model.Project;

@Configuration
@ConfigurationProperties("branch-deployer")
public class Config {

    @Getter @Setter List<Project> projects = new ArrayList<>();
}

package io.evert.branchdeployer.config.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Project {
    @Getter @Setter private String name;
    @Getter @Setter private String webhookSecret;
    @Getter @Setter private String username;
    @Getter @Setter private String password;
}

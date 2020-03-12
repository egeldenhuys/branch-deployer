package io.evert.branchdeployer.config.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Project {
    @Getter @Setter String name;
    @Getter @Setter private String webhookSecret;
}

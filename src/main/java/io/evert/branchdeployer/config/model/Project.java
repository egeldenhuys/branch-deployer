package io.evert.branchdeployer.config.model;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Project {
    // Setters might not be necessary
    @Getter @Setter private String name;
    @ToString.Exclude  @Getter @Setter private String webhookSecret;
    @ToString.Exclude  @Getter @Setter private String username;
    @ToString.Exclude  @Getter @Setter private String password;
    @Getter @Setter private List<String> insertLocalFiles = new ArrayList<>();
}

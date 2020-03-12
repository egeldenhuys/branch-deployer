package io.evert.branchdeployer.webhook;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public abstract class WebhookModel {

    // Install vscode extension `gabrielbb.vscode-lombok` for hints
    @Getter @Setter protected Boolean success;
    @Getter @Setter protected String branchName;
    @Getter @Setter protected String projectName;
    @Getter @Setter protected String commitId;
    @ToString.Exclude  @Getter @Setter protected String webhookSecret;
    @Getter @Setter protected String uri;
    @Getter @Setter protected String pathWithNamepace;
    @Getter @Setter protected Boolean valid;
    @Getter @Setter protected String reason;
    @Getter @Setter protected String status;
    @Getter @Setter protected String commitHash;

    public abstract Boolean init(Map<String, String> headers);

}

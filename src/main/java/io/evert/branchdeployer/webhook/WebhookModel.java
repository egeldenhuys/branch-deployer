package io.evert.branchdeployer.webhook;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public abstract class WebhookModel {

    // Install vscode extension `gabrielbb.vscode-lombok` for hints
    @Getter @Setter protected String status;
    @Getter @Setter protected String branchName;
    @Getter @Setter protected String projectName;
    @Getter @Setter protected String commitId;
    
    public abstract void init();
}

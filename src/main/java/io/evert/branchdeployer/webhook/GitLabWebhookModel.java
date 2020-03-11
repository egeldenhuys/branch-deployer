package io.evert.branchdeployer.webhook;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class GitLabWebhookModel {

    @JsonProperty("object_attributes")
    private Map<String, Object> objectAttributes;

    @JsonProperty("project")
    private Map<String, String> project;

    @JsonProperty("commit")
    private Map<String, Object> commit;

    private String status;
    private String branchName;
    private String projectName;
    private String commitId;

    // public GitLabWebhookModel(Map<String, Object> objectAttributes, Map<String, String> project, Map<String, Object> commit) {
    //     System.out.println(objectAttributes.toString());
    //     this.objectAttributes = objectAttributes;
    //     this.project = project;
    //     this.commit = commit;
    // }

    public void init() {
        this.status = (String)this.objectAttributes.get("status");
        this.branchName = (String)this.objectAttributes.get("ref");
        this.projectName = (String)this.project.get("name");
        this.commitId = (String)this.commit.get("id");
    }
}

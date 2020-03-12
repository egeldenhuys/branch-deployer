package io.evert.branchdeployer.webhook;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class GitLabWebhookModel extends WebhookModel {

    @JsonProperty("object_attributes")
    private Map<String, Object> objectAttributes;

    @JsonProperty("project")
    private Map<String, String> project;

    @JsonProperty("commit")
    private Map<String, Object> commit;

    @Override
    public void init() {
        this.status = (String)this.objectAttributes.get("status");
        this.branchName = (String)this.objectAttributes.get("ref");
        this.projectName = (String)this.project.get("name");
        this.commitId = (String)this.commit.get("id");
    }
}

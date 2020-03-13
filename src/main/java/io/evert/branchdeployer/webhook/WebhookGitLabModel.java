package io.evert.branchdeployer.webhook;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WebhookGitLabModel extends WebhookModel {

    @JsonProperty("object_attributes")
    private Map<String, Object> objectAttributes;

    @JsonProperty("project")
    private Map<String, String> project;

    @JsonProperty("commit")
    private Map<String, Object> commit;

    @Override
    public Boolean init(Map<String, String> headers) {
        if (headers.containsKey("x-gitlab-event") && headers.get("x-gitlab-event").equals("Pipeline Hook")) {
            this.success = this.objectAttributes.get("status").equals("success") ? true : false;
            this.branchName = (String)this.objectAttributes.get("ref");
            this.projectName = (String)this.project.get("name");
            this.commitId = (String)this.commit.get("id");
            this.uri = (String)this.project.get("git_http_url");
            this.pathWithNamepace = (String)this.project.get("path_with_namespace");
            this.status = (String)this.objectAttributes.get("status");
            this.commitHash = (String)this.objectAttributes.get("sha");
            this.valid = true;
        } else {
            this.valid = false;
            this.reason = String.format("Invalid headers: %s", headers);
            return this.valid;
        }

        if (headers.containsKey("x-gitlab-token")) {
            this.webhookSecret = headers.get("x-gitlab-token");
        } else {
            this.webhookSecret = "";
            this.valid = false;
            this.reason = String.format("No webhook secret found in headers: %s", headers);
            return this.valid;
        }

        return this.valid;
    }
}

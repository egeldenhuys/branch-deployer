package io.evert.branchdeployer.webhook;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            this.status = (String)this.objectAttributes.get("status");
            this.branchName = (String)this.objectAttributes.get("ref");
            this.projectName = (String)this.project.get("name");
            this.commitId = (String)this.commit.get("id");
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
        }

        return true;
    }
}
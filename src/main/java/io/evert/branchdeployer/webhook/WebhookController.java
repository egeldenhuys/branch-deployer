package io.evert.branchdeployer.webhook;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

import io.evert.branchdeployer.config.Config;
import io.evert.branchdeployer.config.model.Project;

@Slf4j
@RestController
public class WebhookController {

    @Autowired
    private Config config;

    public Boolean isAllowedProject;

    @PostMapping(value = "/hook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String Test(@RequestHeader final Map<String, String> headers, @RequestBody final String payload)
            throws IOException {
        config.init();

        log.info(String.format("Receieved webhook request. Headers: %s", headers.toString()));
    
        final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        
        WebhookModel webhookModel;

        // Configure webhook based on source
        if (headers.containsKey("x-gitlab-event") && headers.get("x-gitlab-event").equals("Pipeline Hook")) {
            webhookModel = mapper.readValue(payload, GitLabWebhookModel.class);
            if (!webhookModel.init(headers)) {
                log.error("Could not create webhook model");
                return "ERROR_COULD_NOT_PARSE_REQUEST";
            };
        } else {
            log.error(String.format("Webhook source is not supported: %s", headers.toString()));
            return "WEBHOOK_SOURCE_NOT_SUPPORTED";
        }

        // Authenticate webhook
        String webhookSecret = webhookModel.webhookSecret;
        log.debug(webhookSecret);
        Project project = null;
        if (config.getSecretToProjectMap().containsKey(webhookSecret)) {
            project = config.getSecretToProjectMap().get(webhookSecret);
            log.info(String.format("Received webhook for project [%s]", project.getName()));
        } else {
            log.warn(String.format("Webhook Secret [%s] was not found in config", webhookSecret));
            return "INVALID_SECRET";
        }

        return "OK";


        // Admin deploys Branch-Deployer

        // Configures domain suffix
        //  <branch>.<project>.<suffix>
        //  encryption.password-manager.evert.io

        // Admin creates webhook from GitLab to branch-deployer
        //  Configures secret key for auth
        //

        /**
         * Somebody pushes to repo
         * We check auth token
         * Gitlab sends info when build passes/fails
         * If build passes:
         *      
         */
    }
}

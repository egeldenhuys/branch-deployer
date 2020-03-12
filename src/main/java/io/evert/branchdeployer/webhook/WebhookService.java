package io.evert.branchdeployer.webhook;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.evert.branchdeployer.config.BranchDeployerConfig;
import io.evert.branchdeployer.config.model.Project;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class WebhookService {

    @Autowired
    private BranchDeployerConfig config;

    // Get webhook
    public WebhookModel getWebhookFromRequest(Map<String, String> headers, String payload)
            throws IOException {
        final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);
        
        WebhookModel webhookModel;

        // Configure webhook based on source
        if (headers.containsKey("x-gitlab-event") && headers.get("x-gitlab-event").equals("Pipeline Hook")) {
            webhookModel = mapper.readValue(payload, WebhookGitLabModel.class);
            if (!webhookModel.init(headers)) {
                log.error(webhookModel.getReason());
                return null;
            };
        } else {
            log.error(String.format("Webhook source is not supported: %s", headers.toString()));
            return null;
        }

        // Authenticate webhook
        String webhookSecret = webhookModel.webhookSecret;
        Project project = null;
        if (config.getSecretToProjectMap().containsKey(webhookSecret)) {
            project = config.getSecretToProjectMap().get(webhookSecret);
        } else {
            log.warn(String.format("Webhook Secret [%s] was not found in config", webhookSecret));
            return null;
        }

        return webhookModel;
    }
}
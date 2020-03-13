package io.evert.branchdeployer.webhook;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;
import io.evert.branchdeployer.config.BranchDeployerConfig;
import io.evert.branchdeployer.config.model.Project;
import io.evert.branchdeployer.services.BranchDeployerService;

@Slf4j
@RestController
public class WebhookController {

    @Autowired
    BranchDeployerConfig config;

    @Autowired
    WebhookService webhookService;

    @Autowired
    BranchDeployerService branchDeployer;

    @PostMapping(value = "/hook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String handleWebhook(@RequestHeader final Map<String, String> headers, @RequestBody final String payload,
    HttpServletResponse response) {
        
        WebhookModel webhook = null;
        try {
            webhook = webhookService.getWebhookFromRequest(headers, payload);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "ERROR";
        }

        if (webhook == null) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "ERROR";
        }

        if (!webhook.getValid()) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            log.error(webhook.getReason());
            return "ERROR";
        }

        Project project = config.getSecretToProjectMap().get(webhook.getWebhookSecret());
        log.info(String.format("Received webhook for project: %s", project));

        if (!webhook.getSuccess()) {
            log.info(String.format("Skipping deployment due to CI status %s", webhook.getStatus()));
            return "SKIP";
        }

        branchDeployer.asyncDeployBranch(webhook, project);

        return "OK";
    }
}

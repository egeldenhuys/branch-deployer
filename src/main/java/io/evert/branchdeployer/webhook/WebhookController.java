package io.evert.branchdeployer.webhook;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    Config config;

    @Autowired
    WebhookService webhookService;

    @PostMapping(value = "/hook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String handleWebhook(@RequestHeader final Map<String, String> headers, @RequestBody final String payload,
    HttpServletResponse response) {
        
        WebhookModel webhook = null;
        try {
            webhook = webhookService.getWebhookFromRequest(headers, payload);
        } catch (IOException e) {
            log.error(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return "ERROR";
        }

        if (webhook == null) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return "ERROR";
        }

        log.info(String.format("Received webhook for project: %s", config.getSecretToProjectMap().get(webhook.getWebhookSecret())));

        return "OK";
    }
}

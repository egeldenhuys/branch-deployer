package io.evert.branchdeployer.webhook;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@PropertySource("file:config.yml")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    @Value("${webhookSecretToken}")
    private String webhookSecretToken;

    @PostMapping(value = "/hook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String Test(@RequestHeader Map<String, String> headers, @RequestBody String payload)
            throws JsonProcessingException, IOException {
        // Install vscode extension `gabrielbb.vscode-lombok` for hints
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        GitLabWebhookModel webhookModel = mapper.readValue(payload, GitLabWebhookModel.class);
        webhookModel.init();
        log.info(webhookModel.getStatus());
        return "index";
    }
}

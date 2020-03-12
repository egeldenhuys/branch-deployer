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

    @PostMapping(value = "/hook", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String Test(@RequestHeader final Map<String, String> headers, @RequestBody final String payload)
            throws IOException {

        final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                false);

        WebhookModel webhookModel;
        if (headers.containsKey("x-gitlab-event")) {
            webhookModel = mapper.readValue(payload, GitLabWebhookModel.class);
            webhookModel.init();
        } else {
            throw new UnsupportedOperationException(
                    String.format("Webhook source is not supported: %s", headers.toString()));
        }

        List<Project> projects = config.getProjects();
        log.debug(projects.toString());

        return "OK";
    }
}

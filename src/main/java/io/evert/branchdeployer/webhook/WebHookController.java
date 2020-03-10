package io.evert.branchdeployer.webhook;

import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@PropertySource("file:config.yml")
public class WebHookController {

    private static final Logger log = LoggerFactory.getLogger(WebHookController.class);

    @Value("${webhookSecretToken}")
    private String webhookSecretToken;

    @PostMapping(value = "/hook")
    public String Test(@RequestHeader Map<String, String> headers, @RequestBody String payload)
            throws JsonProcessingException {

        log.info(payload);
        Map<String, Object> payloadMap = new ObjectMapper().readValue(payload, Map.class);
        Map<String, Object> repository = (Map) payloadMap.get("repository");
        String name = (String) repository.get("name");
        log.warn(name);

        // headers.forEach((key, value) -> {
        //     log.info(String.format("Header '%s' = %s", key, value));
        // });

        // payload.forEach((key, value) -> {
        //     log.info(String.format("BODY %s = %s", key, value));
        // });

        // log.warn(payload.get("repository").toString());

        // // log.info(payload.toString());

        // // log.warn(payload.get("description").toString());
        // // log.warn(payload.get("repository."))
        return "index";
    }
}

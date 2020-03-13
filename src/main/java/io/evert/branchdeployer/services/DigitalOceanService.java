package io.evert.branchdeployer.services;

import com.myjeeva.digitalocean.DigitalOcean;
import com.myjeeva.digitalocean.exception.DigitalOceanException;
import com.myjeeva.digitalocean.exception.RequestUnsuccessfulException;
import com.myjeeva.digitalocean.impl.DigitalOceanClient;
import com.myjeeva.digitalocean.pojo.DomainRecord;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import io.evert.branchdeployer.config.BranchDeployerConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DigitalOceanService {

    @Autowired
    BranchDeployerConfig config;


    public Boolean createSubDomain(String branchName, String projectName, String domainName) {
        DigitalOcean apiClient = new DigitalOceanClient(config.getDigitalOceanAuthToken());
        String fullName = String.format("%s.%s.%s", branchName, projectName, domainName);

        DomainRecord input = new DomainRecord(String.format("%s.%s", branchName, projectName), config.getCnameValue(),
                "CNAME");
        input.setTtl(60);

        DomainRecord domainRecord = null;
        try {
            domainRecord = apiClient.createDomainRecord(domainName, input);
        } catch (DigitalOceanException e) {
            log.error(String.format("Could not create domain: %s", fullName), e);
            return false;
        } catch (RequestUnsuccessfulException e) {
            log.error(String.format("Could not create domain: %s", fullName), e);
            return false;
        }

        if (domainRecord == null) {
            return false;
        }

        log.info(domainRecord.toString());

        return true;
    }
}

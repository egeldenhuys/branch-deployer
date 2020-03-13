package io.evert.branchdeployer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan
public class BranchDeployerApplication{

	public static void main(String[] args) {
		SpringApplication.run(BranchDeployerApplication.class, args);
	}

}

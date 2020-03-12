package io.evert.branchdeployer.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DockerService {

    public Boolean runCommandRuntime(final String[] commands) {
        // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program
        final Runtime rt = Runtime.getRuntime();
        //final String[] commands = { "system.exe", "-get t" };
        Process proc;
        try {
            proc = rt.exec(commands);
        } catch (IOException e) {
            log.error(String.format("Could not execute command: %s", commands.toString()), e);
            return false;
        }

        final BufferedReader stdInput = new BufferedReader(new 
            InputStreamReader(proc.getInputStream()));

        final BufferedReader stdError = new BufferedReader(new 
            InputStreamReader(proc.getErrorStream()));

        // Read the output from the command
        System.out.println("Here is the standard output of the command:\n");
        String s = null;
        try {
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException e) {
            log.error(String.format("Could not read stdout: %s", commands.toString()), e);
            return false;
        }

        // Read any errors from the attempted command
        System.out.println("Here is the standard error of the command (if any):\n");
        try {
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
        } catch (IOException e) {
            log.error(String.format("Could not read stderr: %s", commands.toString()), e);
            return false;
        }

        return true;
    }

    public Boolean runCommand(File workingDir, String[] commands) {
        // https://mkyong.com/java/how-to-execute-shell-command-from-java/
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commands);
        processBuilder.directory(workingDir);
        processBuilder.inheritIO();
        try {

            Process process = processBuilder.start();

            StringBuilder output = new StringBuilder();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                // log.info("Success!");
            } else {
                log.info(String.format("Exit code: %s", exitVal));
            }

        } catch (IOException e) {
            log.error("IO exception", e);
            return false;
        } catch (InterruptedException e) {
            log.error("InterruptedException", e);
            return false;
        }
        return true;
    }

    public Boolean stackDown(final File dir) {
        log.info(String.format("docker-compose kill in %s", dir.getPath()));
        runCommand(dir, new String[]{"docker-compose", "kill"});
        log.info("DONE");
        return true;
    }

    public Boolean stackUp(final File dir) {
        log.info(String.format("docker-compose up -d in %s", dir.getPath()));
        runCommand(dir, new String[]{"docker-compose", "up", "-d"});
        log.info("DONE");
        return true;
    }
}
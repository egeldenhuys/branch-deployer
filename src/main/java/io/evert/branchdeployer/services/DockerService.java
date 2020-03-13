package io.evert.branchdeployer.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DockerService {

    public Boolean runCommandRuntime(final String[] commands) {
        // https://stackoverflow.com/questions/5711084/java-runtime-getruntime-getting-output-from-executing-a-command-line-program
        final Runtime rt = Runtime.getRuntime();

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

    public Boolean runCommand(File workingDir, String[] commands, Map<String, String> env) {
        // https://mkyong.com/java/how-to-execute-shell-command-from-java/
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(commands);
        processBuilder.directory(workingDir);
        processBuilder.inheritIO();
        Map<String, String> pbEnv = processBuilder.environment();
        env.forEach((k, v) -> pbEnv.put(k, v));

        try {

            Process process = processBuilder.start();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                return true;
            } else {
                log.info(String.format("Exit code: %s", exitVal));
                return false;
            }

        } catch (IOException e) {
            log.error("IO exception", e);
            return false;
        } catch (InterruptedException e) {
            log.error("InterruptedException", e);
            return false;
        }
    }

    public Boolean stackKill(final File dir, Map<String, String> env) {
        log.info(String.format("docker-compose kill in %s", dir.getPath()));
        return runCommand(dir, new String[]{"docker-compose", "kill"}, env);
    }

    public Boolean stackDown(final File dir, Map<String, String> env) {
        stackKill(dir, env);
        log.info(String.format("docker-compose down in %s", dir.getPath()));
        return runCommand(dir, new String[]{"docker-compose", "down"}, env);
    }

    public Boolean stackUp(final File dir, Map<String, String> env) {
        log.info(String.format("docker-compose up -d in %s", dir.getPath()));
        return runCommand(dir, new String[]{"docker-compose", "up", "-d"}, env);
    }
}
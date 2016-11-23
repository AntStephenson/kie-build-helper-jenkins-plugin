/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.jenkinsci.plugins.kieprbuildshelper;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Proc;
import hudson.model.BuildListener;

import java.io.PrintStream;

public class MavenProject {

    private final FilePath projectBasedir;
    private final String mavenHome;
    private final String mavenOpts;
    private final Launcher launcher;
    private final BuildListener listener;

    public MavenProject(FilePath projectBasedir, String mavenHome, String mavenOpts, Launcher launcher, BuildListener listener) {
        this.projectBasedir = projectBasedir;
        this.mavenHome = mavenHome;
        this.mavenOpts = mavenOpts;
        this.launcher = launcher;
        this.listener = listener;
    }

    /**
     * Builds this Maven project using the specified arguments.
     *
     * @param mavenArgLine   Maven argument line with goals, profiles, etc
     * @param envVars        environmental variables passed to the Maven process
     * @param buildLogger    build logger used to print info messages about the progress
     */
    public void build(String mavenArgLine, EnvVars envVars, PrintStream buildLogger) {
        int exitCode;
        try {
            envVars.put("MAVEN_OPTS", mavenOpts);
            buildLogger.println("MAVEN_OPTS=" + envVars.get("MAVEN_OPTS"));
            Proc proc = launcher.launch()
                    .cmdAsSingleString(mavenHome + "/bin/mvn " + mavenArgLine.trim())
                    .envs(envVars)
                    .pwd(projectBasedir)
                    .stdout(listener.getLogger())
                    .stderr(listener.getLogger())
                    .start();
            exitCode = proc.join();
        } catch (Exception e) {
            throw new RuntimeException("Error while executing Maven process!", e);
        }
        if (exitCode != 0) {
            throw new RuntimeException("Error while executing Maven process, non-zero exit code!");
        }
    }
}

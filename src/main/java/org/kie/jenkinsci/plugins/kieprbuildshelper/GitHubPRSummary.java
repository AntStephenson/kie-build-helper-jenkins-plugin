/*
 * Copyright 2015 JBoss by Red Hat
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

import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GitHub;

import java.io.IOException;

public class GitHubPRSummary {

    private final String targetRepo;
    private final String targetRepoOwner;
    private final int id;
    private final String sourceBranch;
    private final String sourceRepoOwner;

    public GitHubPRSummary(String targetRepo, String targetRepoOwner, int id, String sourceBranch, String sourceRepoOwner) {
        this.targetRepo = targetRepo;
        this.targetRepoOwner = targetRepoOwner;
        this.id = id;
        this.sourceBranch = sourceBranch;
        this.sourceRepoOwner = sourceRepoOwner;
    }

    public String getTargetRepo() {
        return targetRepo;
    }

    public String getTargetRepoOwner() {
        return targetRepoOwner;
    }

    public int getId() {
        return id;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public String getSourceRepoOwner() {
        return sourceRepoOwner;
    }

    /**
     * Creates a PR summary from provided link, getting some of the info directly from Github.
     *
     * @param prLink pull request link, e.g. https://github.com/droolsjbpm/drools-wb/pull/77
     * @param github configured Github instance used to talk to Github REST API
     *
     * @return summary info about GitHub PR
     */
    public static GitHubPRSummary fromPRLink(String prLink, String sourceBranch, GitHub github) {
        String str = preProcessPRLink(prLink);
        String[] parts = str.split("/");
        String targetRepoOwner = parts[0];
        String targetRepo = parts[1];
        // parts[2] == "pull", not needed
        int id = Integer.parseInt(parts[3]);
        GHPullRequest pr = null;
        try {
            pr = github.getRepository(targetRepoOwner + "/" + targetRepo).getPullRequest(id);
            String sourceRepoOwner = pr.getHead().getRepository().getOwner().getLogin();
            return new GitHubPRSummary(
                    targetRepo,
                    targetRepoOwner,
                    id,
                    sourceBranch,
                    sourceRepoOwner
            );
        } catch (IOException e) {
            throw new RuntimeException("Error when getting info about PR " + prLink, e);
        }
    }

    /**
     * Pre-processes the PR link, removing the unnecessary parts like "github.com" and trailing slash.
     *
     * @param prLink the full PR link
     *
     * @return part of the PR link that contains the important info (repo owner, repo name and PR ID)
     */
    private static String preProcessPRLink(String prLink) {
        int ghComIdx = prLink.indexOf("github.com");
        if (ghComIdx < 0) {
            throw new IllegalArgumentException("Provided Github PR link '" + prLink + "' is not valid, as it does not contain the string github.com!");
        }
        String noGhComPrInfo = prLink.substring(ghComIdx + "github.com/".length());
        // now the string contains "<repoOwner>/<repoName>/pull/<pullId>"
        if (noGhComPrInfo.endsWith("/")) {
            return noGhComPrInfo.substring(0, noGhComPrInfo.length() - 1);
        } else {
            return noGhComPrInfo;
        }
    }

}

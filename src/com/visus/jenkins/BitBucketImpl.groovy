/*  BitBucketImpl.groovy
 *
 *  Copyright (C) 2022, VISUS Health IT GmbH
 *  This software and supporting documentation were developed by
 *    VISUS Health IT GmbH
 *    Gesundheitscampus-Sued 15
 *    D-44801 Bochum, Germany
 *    http://www.visus.com
 *    mailto:info@visus.com
 *
 *  -> see LICENCE at root of repository
 */
package com.visus.jenkins

import groovy.json.JsonSlurper


/**
 *  Methods for messing with BitBucket
 *
 *  @author Tobias Hahnen
 */
class BitBucketImpl {
    /**
     *  Return the browsable raw URL from a Git URL
     *
     *  @param gitURL to be converted
     *  @return converted URL
     */
    static String rawURL(String gitURL) {
        // http://bitbucket/scm/<Project>/<Repo>.git
        // http://bitbucket/projects/<Project>/repos/<Repo>/raw/
        int i               = gitURL.lastIndexOf("/")
        String rest         = gitURL.substring(0, i)
        String partOfURL    = "/repos/${gitURL.substring(i+1).split("\\.")[0]}/raw/"

        i           = rest.lastIndexOf("/")
        partOfURL   = "/projects/${rest.substring(i+1)}$partOfURL"
        rest        = rest.substring(0, i)

        return "${rest.substring(0, rest.lastIndexOf("/"))}$partOfURL"
    }


    /**
     *  Return the name of the repository from a Git URL
     *
     *  @param gitURL to be used
     *  @return repository name
     */
    static String repoName(String gitURL) {
        // http://bitbucket/scm/<Project>/<Repo>.git
        // <Repo>
        return gitURL.substring(gitURL.lastIndexOf("/")+1).split("\\.")[0]
    }


    /**
     *  Return the name of the project from a Git URL
     *
     *  @param gitURL to be used
     *  @return project name
     */
    static String projectName(String gitURL) {
        return gitURL.substring(gitURL.indexOf("/scm/")+5).split("/")[0]
    }


    /**
     *  Check if a branch also exists as a (not merged nor declined) pull request
     *
     *  @param gitURL to be used
     *  @param branchName to check for opened pull requests
     *  @param username the username to be used in authentication
     *  @param password the password to be used in authentication
     *  @return true if a open pull request exists, false otherwise
     */
    static boolean checkForOpenPullRequest(String gitURL, String branchName, String username, String password) {
        String completeURL = "${gitURL.substring(0, gitURL.indexOf("/scm/"))}/rest/api/latest/projects/" +
                                "${projectName(gitURL)}/repos/${repoName(gitURL)}/pull-requests?limit=100&state=OPEN"

        println("DEBUG 1: completeURL -> $completeURL")

        try {
            // Get results from BitBucket REST API -> { "size": int, "limit": int, ..., "values": List<Object>, ... }
            def result = new JsonSlurper().parseText(
                new URL(completeURL).getText(requestProperties: [
                    'Authorization': 'Basic ' + "${username}:${password}".bytes.encodeBase64().toString()
                ])
            )

            println("DEBUG 2: result -> $result")

            // "values": List<Object> -> { ..., "fromRef": Object, ... } -> { "id": String, ... }
            // -> Path to name of source branch of pull request!
            result.values.forEach {
                if (it.fromRef.id == "refs/heads/${branchName}") {
                    return true
                }
            }
        } catch (Exception ignored) {
            println("DEBUG 3: $ignored")
        }

        return false
    }
}

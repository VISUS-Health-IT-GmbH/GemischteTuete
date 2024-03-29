/*  JenkinsIntegration.groovy
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


import groovy.json.JsonSlurper

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


/**
 *  Returns the initial source (and possible) target branch which are used in the build
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @return tuple containing source / target branch (might be null on branch build)
 */
static String[] getInitialSourceTargetBranches(ctx) {
    return (String[])[
        ctx.env.CHANGE_ID != null ? ctx.env.CHANGE_BRANCH : ctx.env.BRANCH_NAME,
        ctx.env.CHANGE_ID != null ? ctx.env.CHANGE_TARGET : null
    ]
}


/**
 *  Check if a pull request exists in a multi branch pipeline
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param number pull request number
 *  @param username used in authentication
 *  @param password used in authentication
 *  @return true if pull request available in JenkinsIntegration, false otherwise
 */
static boolean checkPRAvailable(ctx, int number, String username, String password) {
    // 1) Feature branch build:
    //    http://jenkins/job/<Pipeline name>/job/<Branch name>/<Build number>/
    // 2) Pull request / merge build:
    //    http://jenkins/job/<Pipeline name>/view/change-requests/job/PR-<Pull request number>/<Build number>/
    if ((ctx.env.BUILD_URL as String).contains("/view/change-requests/job/PR-${number}")) {
        return false
    }

    // Get crumb issued by Jenkins
    String authorization = "Basic ${Base64.encoder.encodeToString("${username}:${password}".getBytes())}"
    String url = "${ctx.env.JENKINS_URL as String}crumbIssuer/api/json"
    HttpRequest request = HttpRequest.newBuilder()
                            .GET()
                            .uri(new URI(url))
                            .header("Authorization", authorization)
                            .build()

    HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    int code = response.statusCode()
    if (code != 200) {
        ctx.echo("!!! [JenkinsIntegration.checkPRAvailable] Could not get crumb from '${url}', exit code '${code}' !!!")
        return false
    }

    // Check if pull request page exists
    Object content = new JsonSlurper().parseText(response.body())
    url = ctx.env.BUILD_URL as String
    url = "${url.substring(0, url.lastIndexOf("/job"))}/view/change-requests/job/PR-${number}"
    request = HttpRequest.newBuilder()
                .GET()
                .uri(new URI(url))
                .header("Authorization", authorization)
                .header(content.crumbRequestField as String, content.crumb as String)
                .build()

    response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
    code = response.statusCode()
    if (code >= 400) {
        ctx.echo("!!! [JenkinsIntegration.checkPRAvailable] Could not get page from '${url}', exit code '${code}' !!!")
        return false
    }
    return true
}


/**
 *  Archives an artifact (or multiple) where the path is either relative or absolute in the workspace directly
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param path the (absolute) file path
 */
static void archiveLocalArtifact(ctx, String path) {
    ctx.archiveArtifacts(
        artifacts: path.substring(path.lastIndexOf(File.separator)+1), fingerprint: true, onlyIfSuccessful: false
    )
}

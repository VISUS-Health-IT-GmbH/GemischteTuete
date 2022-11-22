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
 *  @return true if pull request available in JenkinsIntegration, false otherwise
 */
static boolean checkPRAvailable(ctx, int number) {
    // 1) Feature branch build:
    //    http://jenkins/job/<Pipeline name>/job/<Branch name>/<Build number>/
    // 2) Pull request / merge build:
    //    http://jenkins/job/<Pipeline name>/view/change-requests/job/PR-<Pull request number>/<Build number>/
    String build = ctx.env.BUILD_URL as String
    if (build != null) {
        // invoked from pull request
        if (build.contains("/view/change-requests/job/PR-${number}")) {
            return true
        }

        // check if PR job URL exists
        int code = new URL("${build.substring(0, build.lastIndexOf("/job"))}/view/change-requests/job/PR-${number}")
                    .openConnection()
                    .with { requestMethod = "HEAD"; connect(); responseCode }
        if (code == 200) {
            return true
        } else if (code == 403) {
            ctx.echo("!!! [checkPRAvailable - WARNING] Could not check due to authorization issue (${code}) !!!")
        }
    }

    return false
}

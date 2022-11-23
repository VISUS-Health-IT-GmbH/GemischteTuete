/*  Workspace.groovy
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

import com.visus.jenkins.WorkspaceImpl


/**
 *  Jenkins wrapper for:
 *  @see com.visus.jenkins.WorkspaceImpl#resolveWorkspaceScriptDir
 */
static String resolveWorkspaceScriptDir(String workspace, String gitURL) {
    return WorkspaceImpl.resolveWorkspaceScriptDir(workspace, gitURL)
}


/**
 *  Jenkins wrapper for:
 *  @see com.visus.jenkins.WorkspaceImpl#readFileInWorkspaceScriptDir
 */
static String readFileInWorkspaceScriptDir(String workspaceScriptDir, String filePath) {
    return WorkspaceImpl.readFileInWorkspaceScriptDir(workspaceScriptDir, filePath)
}


/**
 *  Kills all MySQL daemons
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @return exit code
 */
static int killAllMySQLDaemons(ctx) {
    return ctx.bat(
        returnStatus: true,
        script: """WMIC PROCESS WHERE ^(commandline like '%%mysqld%%'^) CALL Terminate 1>nul"""
    )
}


/**
 *  Kills all RMI daemons
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @return exit code
 */
static int killAllRMIDaemons(ctx) {
    return ctx.bat(
        returnStatus: true,
        script: """WMIC PROCESS WHERE ^(name like '%%rmid%%'^) CALL Terminate 1>nul"""
    )
}


/**
 *  Kills all Gradle daemons (possible zombie processes)
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @return exit code
 */
static int killAllGradleDaemons(ctx) {
    return ctx.bat(
        returnStatus: true,
        script: """WMIC PROCESS WHERE ^(commandline like '%%org.gradle%%'^) CALL Terminate 1>nul"""
    )
}


/**
 *  Kills all daemons in a specific order
 *  - MySQL daemons can block Gradle daemons
 *  - Gradle daemons can block RMI daemons
 *  - RMI daemons can block Gradle daemons
 *  ... yaaay ...
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 */
static void killAllDaemons(ctx) {
    killAllMySQLDaemons(ctx)
    killAllGradleDaemons(ctx)
    killAllRMIDaemons(ctx)
    killAllGradleDaemons(ctx)
}


/**
 *  Clean the "build" directory used to share build results between multiple repositories
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @return exit code
 */
static int cleanBuild(ctx) {
    return ctx.bat(
        returnStatus: true,
        script: """del /s /f /q build 1>nul"""
    )
}


/**
 *  Clean the "generateJUnitFiles" directory used to generate jUnit resources shared between multiple repositories
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @return exit code
 */
static int cleanGenerateJUnitFiles(ctx) {
    return ctx.bat(
        returnStatus: true,
        script: """del /s /f /q generateJUnitFiles 1>nul"""
    )
}


/**
 *  Clean the repository provided by name in workspace
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName name of the repository
 *  @return exit code
 */
static int cleanRepository(ctx, String repoName) {
    ctx.dir(repoName) {
        return ctx.bat(
            returnStatus: true,
            script: "git clean -dfx 1>nul"
        )
    }
}


/**
 *  Cleans the complete workspace
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repositories list of all repositories to clean using "cleanRepository(...)"
 */
static void cleanWorkspace(ctx, String[] repositories) {
    cleanBuild(ctx)
    cleanGenerateJUnitFiles(ctx)
    for (String repo in repositories) {
        cleanRepository(ctx, repo)
    }
}


/**
 *  Updates the repository provided by name laying outside the workspace
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param dir absolute / relative directory
 *  @return exit code
 */
static int updateRepositoryOutsideWorkspace(ctx, String dir) {
    return ctx.bat(
        returnStatus: true,
        script: """
            cd /D ${dir} 1>nul || exit /B 1
            git fetch --all --prune 1>nul || exit /B 1
            git pull 1>nul || exit /B 1
        """
    )
}

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
 *  @param logFile the log file to use
 *  @return exit code
 */
static int killAllMySQLDaemons(ctx, String logFile = "C:\\Workspace.killAllMySQLDaemons.log") {
    return ctx.bat(
        returnStatus: true,
        script: """
            echo "WMIC PROCESS WHERE ^(commandline like '%%mysqld%%'^) CALL Terminate" >> ${logFile} 2>&1
            WMIC PROCESS WHERE ^(commandline like '%%mysqld%%'^) CALL Terminate >> ${logFile} 2>&1
            if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
        """
    )
}


/**
 *  Kills all RMI daemons
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param logFile the log file to use
 *  @return exit code
 */
static int killAllRMIDaemons(ctx, String logFile = "C:\\Workspace.killAllRMIDaemons.log") {
    return ctx.bat(
        returnStatus: true,
        script: """
            echo "WMIC PROCESS WHERE ^(name like '%%rmid%%'^) CALL Terminate" >> ${logFile} 2>&1
            WMIC PROCESS WHERE ^(name like '%%rmid%%'^) CALL Terminate >> ${logFile} 2>&1
            if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
        """
    )
}


/**
 *  Kills all Gradle daemons (possible zombie processes)
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param logFile the log file to use
 *  @return exit code
 */
static int killAllGradleDaemons(ctx, String logFile = "C:\\Workspace.killAllGradleDaemons.log") {
    return ctx.bat(
        returnStatus: true,
        script: """
            echo "WMIC PROCESS WHERE ^(commandline like '%%org.gradle%%'^) CALL Terminate" >> ${logFile} 2>&1
            WMIC PROCESS WHERE ^(commandline like '%%org.gradle%%'^) CALL Terminate >> ${logFile} 2>&1
            if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
        """
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
 *  @param logFile the log file to use
 */
static void killAllDaemons(ctx, String logFile = "C:\\Workspace.killAllDaemons.log") {
    killAllMySQLDaemons(ctx, logFile)
    killAllGradleDaemons(ctx, logFile)
    killAllRMIDaemons(ctx, logFile)
    killAllGradleDaemons(ctx, logFile)
}


/**
 *  Clean all log files in workspace created by any build
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @return exit code
 */
static int cleanLogFiles(ctx) {
    return ctx.bat(
        returnStatus: true,
        script: """
            del /s /f /q *.log
            if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
        """
    )
}


/**
 *  Clean the "build" directory used to share build results between multiple repositories
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param logFile the log file to use
 *  @return exit code
 */
static int cleanBuild(ctx, String logFile = "C:\\Workspace.cleanBuild.log") {
    return ctx.bat(
        returnStatus: true,
        script: """
            echo "del /s /f /q build" >> ${logFile} 2>&1
            del /s /f /q build >> ${logFile} 2>&1
            if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
        """
    )
}


/**
 *  Clean the "generateJUnitFiles" directory used to generate jUnit resources shared between multiple repositories
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param logFile the log file to use
 *  @return exit code
 */
static int cleanGenerateJUnitFiles(ctx, String logFile = "C:\\Workspace.cleanGenerateJUnitFiles.log") {
    return ctx.bat(
        returnStatus: true,
        script: """
            echo "del /s /f /q generateJUnitFiles" >> ${logFile} 2>&1
            del /s /f /q generateJUnitFiles >> ${logFile} 2>&1
            if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
        """
    )
}


/**
 *  Clean the repository provided by name in workspace
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName name of the repository
 *  @param logFile the log file to use
 *  @return exit code
 */
static int cleanRepository(ctx, String repoName, String logFile = "C:\\Workspace.cleanRepository.log") {
    ctx.dir(repoName) {
        return ctx.bat(
            returnStatus: true,
            script: """
                echo "git clean -dfx" >> ${logFile} 2>&1
                git clean -dfx >> ${logFile} 2>&1
                if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
            """
        )
    }
}


/**
 *  Cleans the complete workspace
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repositories list of all repositories to clean using "cleanRepository(...)"
 *  @param logFile the log file to use
 */
static void cleanWorkspace(ctx, String[] repositories, String logFile = "C:\\Workspace.cleanWorkspace.log") {
    cleanBuild(ctx, logFile)
    cleanGenerateJUnitFiles(ctx, logFile)
    for (String repo in repositories) {
        cleanRepository(ctx, repo, logFile)
    }
}


/**
 *  Updates the repository provided by name laying outside the workspace
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param dir absolute / relative directory
 *  @param logFile the log file to use
 *  @return exit code
 */
static int updateRepositoryOutsideWorkspace(ctx, String dir,
                                            String logFile = "C:\\Workspace.updateRepositoryOutsideWorkspace.log") {
    return ctx.bat(
        returnStatus: true,
        script: """
            echo "cd /D ${dir}" >> ${logFile} 2>&1
            cd /D ${dir} >> ${logFile} 2>&1
            if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
            echo "git fetch --all --prune" >> ${logFile} 2>&1
            git fetch --all --prune >> ${logFile} 2>&1
            if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
            echo "git pull" >> ${logFile} 2>&1
            git pull >> ${logFile} 2>&1
            if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
        """
    )
}

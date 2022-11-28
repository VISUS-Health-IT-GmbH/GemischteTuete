/*  BitBucket.groovy
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

import java.util.List
import com.visus.jenkins.BitBucketImpl


// TODO: Everywhere where there is a parameter "repoName" also "allow" Git URLs but check if it ends with ".git" and
//       then call (BitBucket.)repoName(...) method!


/**
 *  Jenkins wrapper for:
 *  @see com.visus.jenkins.BitBucketImpl#rawURL
 */
static String rawURL(String gitURL) {
    return BitBucketImpl.rawURL(gitURL)
}


/**
 *  Jenkins wrapper for:
 *  @see com.visus.jenkins.BitBucketImpl#repoName
 */
static String repoName(String gitURL) {
    return BitBucketImpl.repoName(gitURL)
}


/**
 *  Jenkins wrapper for:
 *  @see com.visus.jenkins.BitBucketImpl#projectName
 */
static String projectName(String gitURL) {
    return BitBucketImpl.projectName(gitURL)
}


/**
 *  Jenkins wrapper for:
 *  @see com.visus.jenkins.BitBucketImpl#checkForOpenPullRequest
 */
static int checkForOpenPullRequest(String gitURL, String branchName, String username, String password) {
    return BitBucketImpl.checkForOpenPullRequest(gitURL, branchName, username, password)
}


/**
 *  Clone a Git repository if not already exists
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param gitURL repository ".git" URL
 *  @param logFile the log file to use
 *  @return exit code
 */
static int clone(ctx, String gitURL, String logFile = "C:\\BitBucket.clone.log") {
    if (!ctx.fileExists(repoName(gitURL))) {
        return ctx.bat(
            returnStatus: true,
            script: """
                echo "git clone ${gitURL}" >> ${logFile} 2>&1
                git clone ${gitURL} >> ${logFile} 2>&1
                if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
            """
        )
    }
    return 0
}


/**
 *  Checks a Git branch out in the repository provided
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName the name of the directory in workspace
 *  @param branchName the branch to be checked out
 *  @param LFS whether the repository is a Git LFS repository requires invoking additional commands
 *  @param logFile the log file to use
 *  @return exit code
 */
static int checkout(ctx, String repoName, String branchName, Boolean LFS,
                    String logFile = "C:\\BitBucket.checkout.log") {
    ctx.dir(repoName) {
        // i) abort possible previous MERGING state
        ctx.bat(
            returnStatus: true,
            script: """
                echo "git merge --abort" >> ${logFile} 2>&1
                git merge --abort >> ${logFile} 2>&1
                if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
            """
        )

        // ii) initial clean of the repository
        def exitCode = ctx.bat(
            returnStatus: true,
            script: """
                echo "git fetch --all --prune" >> ${logFile} 2>&1
                git fetch --all --prune >> ${logFile} 2>&1
                if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                echo "git clean -dfx" >> ${logFile} 2>&1
                git clean -dfx >> ${logFile} 2>&1
                if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                echo "git gc --auto --quiet" >> ${logFile} 2>&1
                git gc --auto --quiet >> ${logFile} 2>&1
                if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
            """
        )

        // iii) checkout & Git submodules
        if (exitCode == 0) {
            exitCode = ctx.bat(
                returnStatus: true,
                script: """
                    echo "git checkout ${branchName}" >> ${logFile} 2>&1
                    git checkout ${branchName} >> ${logFile} 2>&1
                    if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                """
            )
            if (exitCode > 0 && LFS) {
                // INFO: An issue with Git LFS pointers has occurred, stash the changes (including dropping them) and
                //       clean the repository again, starting at the last clean commit!
                ctx.bat(
                    returnStatus: true,
                    script: """
                        echo "git stash --include-untracked" >> ${logFile} 2>&1
                        git stash --include-untracked >> ${logFile} 2>&1
                        if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                        echo "git clean -dfx" >> ${logFile} 2>&1
                        git clean -dfx >> ${logFile} 2>&1
                        if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                        echo "git reset --hard" >> ${logFile} 2>&1
                        git reset --hard >> ${logFile} 2>&1
                        if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                        echo "git stash clear" >> ${logFile} 2>&1
                        git stash clear >> ${logFile} 2>&1
                        if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                        echo "git checkout ${branchName}" >> ${logFile} 2>&1
                        git checkout ${branchName} >> ${logFile} 2>&1
                        if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                    """
                )
            }

            exitCode = ctx.bat(
                returnStatus: true,
                script: """
                    echo "git pull" >> ${logFile} 2>&1
                    git pull >> ${logFile} 2>&1
                    if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                    echo "git submodule update --init --recursive" >> ${logFile} 2>&1
                    git submodule update --init --recursive >> ${logFile} 2>&1
                    if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                """
            )
            if (exitCode > 0) {
                // INFO: An issue with Git where the same branch differs on remote and locally even though no commits
                //       where done locally -> reset hard to the remote version of the branch requested!
                ctx.bat(
                    returnStatus: true,
                    script: """
                    echo "git reset --hard origin/${branchName}" >> ${logFile} 2>&1
                    git reset --hard origin/${branchName} >> ${logFile} 2>&1
                    if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                    echo "git pull" >> ${logFile} 2>&1
                    git pull >> ${logFile} 2>&1
                    if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                    echo "git submodule update --init --recursive" >> ${logFile} 2>&1
                    git submodule update --init --recursive >> ${logFile} 2>&1
                    if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                    """
                )
            }
        }

        // iv) Git LFS if required
        if (LFS && exitCode == 0) {
            exitCode = ctx.bat(
                returnStatus: true,
                script: """
                    echo "git lfs fetch --all --prune" >> ${logFile} 2>&1
                    git lfs fetch --all --prune >> ${logFile} 2>&1
                    if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                    echo "git lfs pull" >> ${logFile} 2>&1
                    git lfs pull >> ${logFile} 2>&1
                    if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
                """
            )
        }

        return exitCode
    }
}


/**
 *  Merge a Git branch into the current one
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName the name of the directory in workspace
 *  @param branchName the branch to merge into the current one
 *  @param logFile the log file to use
 *  @return exit code
 */
static int merge(ctx, String repoName, String branchName, String logFile = "C:\\BitBucket.merge.log") {
    ctx.dir(repoName) {
        return ctx.bat(
            returnStatus: true,
            script: """
                echo "git merge ${branchName}" >> ${logFile} 2>&1
                git merge ${branchName} >> ${logFile} 2>&1
                if %ERRORLEVEL% neq 0 exit /B %ERRORLEVEL%
            """
        )
    }
}


/**
 *  Initialize a repository based on information provided by multibranch pipeline: branch or pull request build
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName the name of the directory in workspace
 *  @param source the base source branch
 *  @param target the base target branch (can be null on branch build)
 *  @param fallback branch name on which to fallback (e.g. "develop")
 *  @param LFS whether the repository is a Git LFS repository requires invoking additional commands
 *  @param logFile the log file to use
 *  @return tuple containing source / target branch, source branch might differ from multibranch pipeline branch and
 *          target can be null on branch build
 */
static String[] init(ctx, String repoName, String source, String target, String fallback, Boolean LFS,
                     String logFile = "C:\\BitBucket.init.log") {
    // checkout if source / target branch exist (assuming fallback exists all the time, e.g. the main branch)
    // 1) TARGET: target is null -> null / target found -> target / target missing -> fallback
    // 2) SOURCE: source found -> source / target found -> target / target missing -> fallback
    List<String> branches = null
    ctx.dir(repoName) {
        branches = (ctx.bat(returnStdout: true, script: "git ls-remote --heads") as String)
                    .split()
                    .findAll { line -> line.contains("refs/heads/") }
                    .collect { line -> line.replace("refs/heads/", "").strip() }
    }

    String usedTarget = target == null ? target : (branches.contains(target) ? target : fallback)
    String usedSource = branches.contains(source) ? source : (target != null ? target : fallback)

    // checkout source branch
    int exit = checkout(ctx, repoName, usedSource, LFS, logFile)
    if (exit > 0) {
        ctx.error(
            message: """[BitBucket.init] Checking out source branch (${usedSource}) failed with exit code: ${exit}!"""
        )
    }

    // checkout target branch (if PR / merge build and source branch does not equal target branch)
    if (usedTarget != null && usedSource != usedTarget) {
        exit = checkout(ctx, repoName, usedTarget, LFS, logFile)
        if (exit > 0) {
            ctx.error(
                message: "[BitBucket.init] Checking out target branch (${usedTarget}) failed with exit code: ${exit}!"
            )
        }

        exit = merge(ctx, repoName, usedSource, logFile)
        if (exit > 0) {
            ctx.error(message: """
                [BitBucket.init] Merging '${usedSource}' into '${usedTarget}' failed with exit code: ${exit}!
            """.stripIndent())
        }

        ctx.echo("!!! [BitBucket.init] Merge build: ${repoName} -> ${usedSource} merged into ${usedTarget} !!!")
    } else if (usedTarget != null) {
        ctx.echo(
            "!!! [BitBucket.init] Merge build: ${repoName} -> source (${usedSource}) == target (${usedTarget}) !!!"
        )
    } else {
        ctx.echo("!!! [BitBucket.init] Branch build: ${repoName} -> ${usedSource} !!!")
    }

    return (String[])[usedSource, usedTarget]
}


/**
 *  Get the last Git commit hash of a specific repository in workspace
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName the repository to check last commit hash
 *  @return commit hash
 */
static String lastCommitHash(ctx, String repoName) {
    ctx.dir(repoName) {
        String hash = ctx.bat(
            returnStdout: true,
            script: """git log -n 1 --pretty=format:'%%H'"""
        ).trim()
        return hash.substring(hash.lastIndexOf("\n")).trim().replaceAll("'", "")
    }
}

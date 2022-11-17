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
 *  @return exit code
 */
static int clone(ctx, String gitURL) {
    if (!ctx.fileExists(repoName(gitURL))) {
        return ctx.bat(returnStatus: true, script: "git clone ${gitURL} 1>nul || exit /B 1")
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
 *  @return exit code
 */
static int checkout(ctx, String repoName, String branchName, Boolean LFS = false) {
    ctx.dir(repoName) {
        // i) abort possible previous MERGING state
        ctx.bat(returnStatus: true, script: "git merge --abort 1>nul")

        // ii) initial clean of the repository
        def exitCode = ctx.bat(
            returnStatus: true,
            script: """
                git fetch --all --prune 1>nul || exit /B 1
                git clean -dfx 1>nul || exit /B 1
                git gc --auto --quiet 1>nul || exit /B 1
            """
        )

        // iii) checkout & Git submodules
        if (exitCode == 0) {
            exitCode = ctx.bat(returnStatus: true, script: "git checkout ${branchName} 1>nul")
            if (exitCode > 0 && LFS) {
                // INFO: An issue with Git LFS pointers has occurred, stash the changes (including dropping them) and
                //       clean the repository again, starting at the last clean commit!
                ctx.bat(
                    returnStatus: true,
                    script: """
                        git stash --include-untracked 1>nul || exit /B 1
                        git clean -dfx 1>nul || exit /B 1
                        git reset --hard 1>nul || exit /B 1
                        git stash clear 1>nul || exit /B 1
                        git checkout ${branchName} 1>nul || exit /B 1
                    """
                )
            }

            exitCode = ctx.bat(
                returnStatus: true,
                script: """
                    git pull 1>nul || exit /B 1
                    git submodule update --init --recursive 1>nul || exit /B 1
                """
            )
        }

        // iv) Git LFS if required
        if (LFS && exitCode == 0) {
            exitCode = ctx.bat(
                returnStatus: true,
                script: """
                    git lfs fetch --all --prune 1>nul || exit /B 1
                    git lfs pull 1>nul || exit /B 1
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
 *  @return exit code
 */
static int merge(ctx, String repoName, String branchName) {
    ctx.dir(repoName) {
        return ctx.bat(returnStatus: true, script: "git merge ${branchName} || exit /B 1")
    }
}


/**
 *  Initialize a repository based on information provided by multibranch pipeline: branch or pull request build
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName the name of the directory in workspace
 *  @param fallback branch name on which to fallback (e.g. "develop")
 *  @param LFS whether the repository is a Git LFS repository requires invoking additional commands
 *  @return tuple containing source / target branch, source branch might differ from multibranch pipeline branch and
 *          target can be null on branch build
 */
static String[] init(ctx, String repoName, String fallback, Boolean LFS = false) {
    // load variables to check if either PR or normal build
    String source = ctx.env.CHANGE_ID != null ? ctx.env.CHANGE_BRANCH : ctx.env.BRANCH_NAME
    String target = ctx.env.CHANGE_ID != null ? ctx.env.CHANGE_TARGET : null

    // checkout if source / target branch exist (assuming fallback exists all the time, e.g. the main branch)
    // 1) target exists and target not in branches -> use fallback
    // 2) target exists and source not in branches -> use target
    // 3) target does not exist and source not in branches -> use fallback
    List<String> branches = null
    ctx.dir(repoName) {
        branches = (ctx.bat(returnStdout: true, script: """git branch -r""") as String)
                    .split("\n").collect { line -> line.strip() }
    }
    target = target != null && !branches.contains(target) ? fallback : target
    source = target != null && !branches.contains(source) ? target : source
    source = target == null && !branches.contains(source) ? fallback : source

    // checkout source branch
    int exit = checkout(ctx, repoName, source, LFS)
    if (exit > 0) {
        ctx.error(message: """
            Checking out source branch (${source}) failed with exit code: ${exit}!
        """.stripIndent())
    }

    // checkout target branch (if PR / merge build and source branch does not equal target branch)
    if (target != null && source != target) {
        exit = checkout(ctx, repoName, target, LFS)
        if (exit > 0) {
            ctx.error(message: "[ERROR] Checking out target branch (${target}) failed with exit code: ${exit}!")
        }

        exit = merge(ctx, repoName, source)
        if (exit > 0) {
            ctx.error(message: """
                [ERROR] Merging source branch (${source}) into target branch (${target}) failed with exit code: ${exit}!
            """.stripIndent())
        }

        ctx.echo("!!! Merge build: ${repoName} -> ${source} merged into ${target} !!!")
    } else if (target != null) {
        ctx.echo("!!! Merge build: ${repoName} -> with source branch (${source}) equals target branch (${target}) !!!")
    } else {
        ctx.echo("!!! Branch build: ${repoName} -> ${source} !!!")
    }

    return (String[])[source, target]
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

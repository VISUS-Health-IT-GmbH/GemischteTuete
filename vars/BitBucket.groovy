/*  BitBucket.groovy
 *
 *  Copyright (C) 2022, VISUS Health IT GmbH
 *  This software and supporting documentation were developed by
 *    VISUS Health IT GmbH
 *    Gesundheitscampus-Sued 15-17
 *    D-44801 Bochum, Germany
 *    http://www.visus.com
 *    mailto:info@visus.com
 *
 *  -> see LICENCE at root of repository
 */
import com.visus.jenkins.BitBucketImpl


/**
 *  Jenkins wrapper for:
 *  @see com.visus.jenkins.BitBucketImpl#rawURL
 */
static String rawURL(String gitURL) {
    BitBucketImpl.rawURL(gitURL)
}


/**
 *  Jenkins wrapper for:
 *  @see com.visus.jenkins.BitBucketImpl#repoName
 */
static String repoName(String gitURL) {
    BitBucketImpl.repoName(gitURL)
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
        return ctx.bat(returnStatus: true, script: "git clone ${gitURL} 1>nul 2>&1 || exit /B 1")
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
        ctx.bat(returnStatus: true, script: "git merge --abort 1>nul 2>&1")

        // ii) initial clean of the repository
        def exitCode = ctx.bat(
            returnStatus: true,
            script: """
                :: fetch changes from server and prune everything unnecessary + clean
                git fetch --all --prune 1>nul 2>&1 || exit /B 1
                git clean -dfx 1>nul 2>&1 || exit /B 1
                git gc --auto --quiet 1>nul 2>&1 || exit /B 1
            """
        )

        // iii) checkout & Git submodules
        if (exitCode == 0) {
            exitCode = ctx.bat(
                returnStatus: true,
                script: """
                    :: checkout the specific branch, pull possible changes and update Git sub modules if any found
                    git checkout ${branchName} 1>nul 2>&1 || exit /B 1
                    git pull 1>nul 2>&1 || exit /B 1
                    git submodule update --init --recursive 1>nul 2>&1 || exit /B 1
                """
            )
        }

        // iv) Git LFS if required
        if (LFS && exitCode == 0) {
            exitCode = ctx.bat(
                returnStatus: true,
                script: """
                    :: Git LFS operations
                    git lfs fetch --all --prune 1>nul 2>&1 || exit /B 1
                    git lfs pull 1>nul 2>&1 || exit /B 1
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
static Tuple2<String, String> init(ctx, String repoName, String fallback, Boolean LFS = false) {
    // load variables to check if either PR or normal build
    String source = ctx.env.CHANGE_ID != null ? ctx.env.CHANGE_BRANCH : ctx.env.BRANCH_NAME
    String target = ctx.env.CHANGE_ID != null ? ctx.env.CHANGE_TARGET : null

    // checkout source branch
    int exit = checkout(ctx, repoName, source, LFS)
    if (exit > 0 && target != null) {
        exit = checkout(ctx, repoName, target, LFS)
        source = exit == 0 ? target : source
    }
    if (exit > 0) {
        exit = checkout(ctx, repoName, fallback, LFS)
        source = exit == 0 ? fallback : source
    }
    if (exit > 0) {
        ctx.error(message: """
            Checking out source branch (${source}) failed with exit code: ${exit}!
        """.stripIndent())
    }

    // checkout target branch & merge on PR
    if (target != null) {
        exit = checkout(ctx, repoName, target, LFS)
        if (exit > 0) {
            exit = checkout(ctx, repoName, fallback, LFS)
            target = exit == 0 ? fallback : target
        }
        if (exit > 0) {
            ctx.error(message: """
                Checking out target branch (${target}) failed with exit code: ${exit}!
            """.stripIndent())
        }

        exit = merge(ctx, repoName, source)
        if (exit > 0) {
            ctx.error(message: """
                Merging source branch (${source}) into target branch (${target}) failed with exit code: ${exit}!
            """.stripIndent())
        }

        ctx.echo("!!! Merge build: ${repoName} -> ${source} merged into ${target} !!!")
    } else {
        ctx.echo("!!! Branch build: ${repoName} -> ${source} !!!")
    }

    return new Tuple2<String, String>(source, target)
}

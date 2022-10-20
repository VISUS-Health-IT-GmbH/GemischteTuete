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
 *  @param gitURL repository ".git" URL
 *  @return exit code
 */
static int clone(String gitURL) {
    if (!fileExists(repoName(gitURL))) {
        return bat(returnStatus: true, script: "git clone ${gitURL} 1>nul 2>&1 || exit /B 1")
    }
    return 0
}


/**
 *  Merge a Git branch into the current one
 *
 *  @param repoName the name of the directory in workspace
 *  @param branchName the branch to merge into the current one
 *  @return exit code
 */
static int merge(String repoName, String branchName) {
    dir(repoName) {
        return bat(returnStatus: true, script: "git merge ${branchName} || exit /B 1")
    }
}

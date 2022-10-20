/*  BitBucketImpl.groovy
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
package com.visus.jenkins


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
}

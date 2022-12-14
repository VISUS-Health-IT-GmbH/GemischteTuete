/*  BitBucketImplTest.groovy
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

import org.junit.Assert
import org.junit.Test


/** jUnit tests on BitBucketImpl */
class BitBucketImplTest {
    /** 1) Test "rawURL" method */
    @Test void test_rawURL() {
        Assert.assertEquals(
            "http://bitbucket/projects/GitHub/repos/JenkinsBib/raw/",
            BitBucketImpl.rawURL("http://bitbucket/scm/GitHub/JenkinsBib.git")
        )
    }


    /** 2) Test "repoName" method */
    @Test void test_repoName() {
        Assert.assertEquals(
            "JenkinsBib",
            BitBucketImpl.repoName("http://bitbucket/scm/GitHub/JenkinsBib.git")
        )
    }


    /** 3) Test "projectName" method */
    @Test void test_projectName() {
        Assert.assertEquals(
            "GitHub",
            BitBucketImpl.projectName("http://bitbucket/scm/GitHub/JenkinsBib.git")
        )
    }


    /** 4) Test "checkForOpenPullRequest" method */
    @Test void test_checkForOpenPullRequest() {
        Assert.assertEquals(
            -1, BitBucketImpl.checkForOpenPullRequest("http://bitbucket/scm/GitHub/JenkinsBib.git","develop", "a", "b")
        )
    }


    /** 5) Test "developOrReleaseBranch" method */
    @Test void test_developOrReleaseBranch() {
        Assert.assertTrue(BitBucketImpl.developOrReleaseBranch("develop"))
        Assert.assertTrue(BitBucketImpl.developOrReleaseBranch("release/JenkinsBib/1.0"))
        Assert.assertFalse(BitBucketImpl.developOrReleaseBranch("niceBranchName"))
    }
}

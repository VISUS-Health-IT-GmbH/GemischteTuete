/*  BitBucketImplTest.groovy
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
}

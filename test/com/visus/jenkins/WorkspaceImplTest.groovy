/*  WorkspaceImplTest.groovy
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


/** jUnit tests on WorkspaceImpl */
class WorkspaceImplTest {
    /** 1) Test "resolveWorkspaceScriptDir" method which results in the raw URL */
    @Test void test_resolveWorkspaceScriptDir_URL() {
        Assert.assertEquals(
            BitBucketImpl.rawURL("http://bitbucket/scm/GitHub/JenkinsBib.git"),
            WorkspaceImpl.resolveWorkspaceScriptDir(
                (new File("C:\\")).absolutePath, "http://bitbucket/scm/GitHub/JenkinsBib.git"
            )
        )
    }


    /** 2) Test "resolveWorkspaceScriptDir" method which results in folder */
    @Test void test_resolveWorkspaceScriptDir_File() {
        File testDir = new File("C:\\", "workspace@script")
        File testDirContent = new File(testDir, "abcdef")
        testDirContent.mkdirs()

        Assert.assertEquals(
            "${testDirContent.absolutePath}/".toString(),
            WorkspaceImpl.resolveWorkspaceScriptDir(
                (new File("C:\\", "workspace")).absolutePath, "https://abc"
            )
        )

        testDirContent.delete()
        testDir.delete()
    }


    /** 3) Test "readFileInWorkspaceScriptDir" method with URL as workspace */
    @Test void test_readFileInWorkspaceScriptDir_URL() {
        Assert.assertEquals(
            (new URL("https://raw.githubusercontent.com/VISUS-Health-IT-GmbH/GemischteTuete/main/README.md")).text,
            WorkspaceImpl.readFileInWorkspaceScriptDir(
                "https://raw.githubusercontent.com/VISUS-Health-IT-GmbH/GemischteTuete/main/",
                "README.md"
            )
        )
    }


    /** 4) Test "readFileInWorkspaceScriptDir" method with folder as workspace */
    @Test void test_readFileInWorkspaceScriptDir_File() {
        Assert.assertEquals(
            (new File("C:\\Windows\\System32\\drivers\\etc\\hosts")).text,
            WorkspaceImpl.readFileInWorkspaceScriptDir("C:\\Windows\\System32\\drivers\\etc\\", "hosts")
        )
    }
}

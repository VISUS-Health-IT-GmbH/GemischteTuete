/*  WorkspaceImpl.groovy
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
 *  Methods for messing with the Jenkins Workspace
 *
 *  @author Tobias Hahnen
 */
class WorkspaceImpl {
    /**
     *  Tries to resolve the workspace script dir -> necessary when reading in file in "parameters" block
     *
     *  @param workspace most likely read in using env.WORKSPACE
     *  @param gitURL the Git repository URL
     *  @return directory path if found otherwise fallback to URL
     */
    static String resolveWorkspaceScriptDir(String workspace, String gitURL) {
        File[] possibleDirs = (new File("$workspace@script")).listFiles().findAll {
            it.isDirectory() && !it.name.endsWith("@tmp")
        }

        return possibleDirs.size() > 0 ? "${possibleDirs.first().absolutePath}/" : BitBucketImpl.rawURL(gitURL)
    }


    /**
     *  Tries to read a file from workspace script dir -> necessary in "parameters" block
     *
     *  @param workspaceScriptDir the resolved workspace script directory
     *  @param filePath the path to the file to be read
     *  @return the content of the file (either from file or URL)
     */
    static String readFileInWorkspaceScriptDir(String workspaceScriptDir, String filePath) {
        return (
            workspaceScriptDir.startsWith("http")
                ? new URL("$workspaceScriptDir$filePath").text
                : new File("$workspaceScriptDir$filePath").text
        )
    }
}

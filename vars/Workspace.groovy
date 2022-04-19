/*  Workspace.groovy
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

import com.visus.jenkins.WorkspaceImpl


/**
 *  Jenkins wrapper for:
 *  @see com.visus.jenkins.WorkspaceImpl
 */
static String resolveWorkspaceScriptDir(String workspace, String gitURL) {
    return WorkspaceImpl.resolveWorkspaceScriptDir(workspace, gitURL)
}


/**
 *  Jenkins wrapper for:
 *  @see com.visus.jenkins.WorkspaceImpl
 */
static String readFileInWorkspaceScriptDir(String workspaceScriptDir, String filePath) {
    return WorkspaceImpl.readFileInWorkspaceScriptDir(workspaceScriptDir, filePath)
}

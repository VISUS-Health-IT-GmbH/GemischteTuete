/*  Gradle.groovy
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


/**
 *  Runs Gradle tasks using arguments provided in a specific environment to catch errors and also find Gradle build
 *  scans (requires the Gradle Plugin in Jenkins).
 *
 *  @param ctx ctx Jenkinsfile context to invoke DSL commands
 *  @param dir directory to run Gradle in
 *  @param list of Gradle tasks
 *  @param args arguments passed to Gradle task call
 *  @param stage result of the stage on error, defaults to FAILURE
 *  @param build result of the build on error, defaults to FAILURE
 */
static void run(ctx, String dir, String[] tasks, String args, String stage = "FAILURE", String build = "FAILURE") {
    ctx.dir(dir) {
        ctx.withGradle {
            ctx.catchError(stageResult: stage, buildResult: build) {
                ctx.bat(script: """call gradlew.bat ${tasks.join(" ")} ${args}""")
            }
        }
    }
}


/**
 *  Runs the "jar" and "testClasses" task in Gradle
 *  (see: https://docs.gradle.org/current/userguide/java_plugin.html#java_plugin)
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName name of the repository
 *  @param args arguments passed to Gradle task call
 *  @param stage result of the stage on error, defaults to FAILURE
 *  @param build result of the build on error, defaults to FAILURE
 */
static void JarTestClasses(ctx, String repoName, String args, String stage = "FAILURE", String build = "FAILURE") {
    run(ctx, repoName, (String[])["jar", "testClasses"], args, stage, build)
}


/**
 *  Runs the "war" and "testClasses" task in Gradle
 *  (see: https://docs.gradle.org/current/userguide/java_plugin.html#java_plugin)
 *  (see: https://docs.gradle.org/current/userguide/war_plugin.html#war_plugin)
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName name of the repository
 *  @param args arguments passed to Gradle task call
 *  @param stage result of the stage on error, defaults to FAILURE
 *  @param build result of the build on error, defaults to FAILURE
 */
static void WarTestClasses(ctx, String repoName, String args, String stage = "FAILURE", String build = "FAILURE") {
    run(ctx, repoName, (String[])["war", "testClasses"], args, stage, build)
}


/**
 *  Runs the "jar", "war" and "testClasses" task in Gradle
 *  (see: https://docs.gradle.org/current/userguide/java_plugin.html#java_plugin)
 *  (see: https://docs.gradle.org/current/userguide/war_plugin.html#war_plugin)
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName name of the repository
 *  @param args arguments passed to Gradle task call
 *  @param stage result of the stage on error, defaults to FAILURE
 *  @param build result of the build on error, defaults to FAILURE
 */
static void JarWarTestClasses(ctx, String repoName, String args, String stage = "FAILURE", String build = "FAILURE") {
    run(ctx, repoName, (String[])["jar", "war", "testClasses"], args, stage, build)
}


/**
 *  Runs the "test" task in Gradle (see: https://docs.gradle.org/current/userguide/java_plugin.html#java_plugin)
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName name of the repository
 *  @param args arguments passed to Gradle task call
 *  @param stage result of the stage on error, defaults to FAILURE
 *  @param build result of the build on error, defaults to FAILURE
 */
static void Test(ctx, String repoName, String args, String stage = "FAILURE", String build = "FAILURE") {
    run(ctx, repoName, (String[])["test"], args, stage, build)
}


/**
 *  Runs the "publishJUnitResults" task in Gradle (see: https://github.com/VISUS-Health-IT-GmbH/jUnitReportsPlugin)
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName name of the repository
 *  @param args arguments passed to Gradle task call
 *  @param stage result of the stage on error, defaults to FAILURE
 *  @param build result of the build on error, defaults to FAILURE
 */
static void PublishJUnitResults(ctx, String repoName, String args, String stage = "FAILURE", String build = "FAILURE") {
    run(ctx, repoName, (String[])["publishJUnitResults"], args, stage, build)
}


/**
 *  Runs the "jaCoCoTestReport" task in Gradle
 *  (see: https://docs.gradle.org/current/userguide/jacoco_plugin.html#jacoco_plugin)
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName name of the repository
 *  @param args arguments passed to Gradle task call
 *  @param stage result of the stage on error, defaults to FAILURE
 *  @param build result of the build on error, defaults to FAILURE
 */
static void JaCoCoTestReport(ctx, String repoName, String args, String stage = "FAILURE", String build = "FAILURE") {
    run(ctx, repoName, (String[])["jaCoCoTestReport"], args, stage, build)
}


/**
 *  Runs the "sonarqube" task in Gradle (see: https://plugins.gradle.org/plugin/org.sonarqube)
 *
 *  @param ctx Jenkinsfile context to invoke DSL commands
 *  @param repoName name of the repository
 *  @param args arguments passed to Gradle task call
 *  @param stage result of the stage on error, defaults to UNSTABLE
 *  @param build result of the build on error, defaults to UNSTABLE
 */
static void SonarQube(ctx, String repoName, String args, String stage = "UNSTABLE", String build = "UNSTABLE") {
    run(ctx, repoName, (String[])["sonarqube"], args, stage, build)
}

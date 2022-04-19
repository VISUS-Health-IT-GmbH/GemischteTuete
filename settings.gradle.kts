/// settings.gradle.kts (Jenkins shared library):
/// ============================================
///
/// Access gradle.properties:
///     yes -> "val prop_name = settings.extra['prop.name']"
///     no  -> "val prop_name = String by settings"


/** 1) Configuration for buildscript plugin dependencies */
pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}


/** 2) Set library name */
rootProject.name = settings.extra["library.name"]!! as String

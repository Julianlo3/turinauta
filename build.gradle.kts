// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}

// Los archivos generados fuera de OneDrive evitan que la sincronización bloquee los .dex.
val buildKey = Integer.toHexString(rootDir.canonicalPath.lowercase().hashCode())
val localBuildRoot = File(gradle.gradleUserHomeDir, "turinauta-build/$buildKey")
layout.buildDirectory.set(localBuildRoot.resolve("root"))
subprojects {
    layout.buildDirectory.set(localBuildRoot.resolve(name))
}

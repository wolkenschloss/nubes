buildscript {
}

// see https://youtrack.jetbrains.com/issue/KTIJ-19369
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.ide) apply false
}

val ideaPlugin = libs.plugins.ide
allprojects {
    apply(plugin = ideaPlugin.get().pluginId)
}

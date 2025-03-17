pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GovSecureID"

include(":app")

// Add core
include(":core")
project(":core").projectDir = File("tiqr-app-core-android/core")

// Add data
include(":data")
project(":data").projectDir = File("tiqr-app-core-android/data")
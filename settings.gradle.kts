pluginManagement {
    repositories {
//        google {
//            content {
//                includeGroupByRegex("com\\.android.*")
//                includeGroupByRegex("com\\.google.*")
//                includeGroupByRegex("androidx.*")
//            }
//        }
//        mavenCentral()
        maven {
            //   setUrl("https://maven.google.com")
            url =uri("https://maven.myket.ir")
        }
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
//        google()
//        mavenCentral()
//        mavenLocal()
//        maven {
//            url = uri("https://dl-maven-android.mintegral.com/repository/mbridge_android_sdk_oversea")
//        }
        maven {
            //   setUrl("https://maven.google.com")
            url =uri("https://maven.myket.ir")
        }
    }
}

rootProject.name = "easy_contact"
include(":app")
 
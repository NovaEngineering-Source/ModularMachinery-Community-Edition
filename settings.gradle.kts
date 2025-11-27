//pluginManagement {
//    repositories {
//        maven { url = 'https://maven.minecraftforge.net/' }
//        maven {
//            name 'Garden of Fancy'
//            url 'https://maven.gofancy.wtf/releases'
//        }
//        gradlePluginPortal()
//    }
//}
pluginManagement {
    repositories {
        maven {
            // RetroFuturaGradle
            name = "GTNH Maven"
            url = uri("https://nexus.gtnewhorizons.com/repository/public/")
            isAllowInsecureProtocol = true
            mavenContent {
                includeGroupByRegex("com\\.gtnewhorizons\\..+")
                includeGroup("com.gtnewhorizons")
            }
        }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

plugins {
    // Automatic toolchain provisioning
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
}

rootProject.name = "ModularMachinery-CE"

import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    java
    id("org.jetbrains.intellij.platform") version "2.1.0"
    id("org.jetbrains.kotlin.jvm")version "1.9.24"
}

group = "org.jetbrains"
version = "1.16"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    testImplementation("junit:junit:4.13.2")

    intellijPlatform {
        create("IU", "2024.1.1")
        plugins("AceJump:3.8.20")
        plugins("IdeaVIM:2.16.0")

        pluginVerifier()
        zipSigner()
        instrumentationTools()

        testFramework(TestFrameworkType.Platform)
        testFramework(TestFrameworkType.JUnit5)
    }
}

val publishToken: String by project

intellijPlatform {
    publishing {
        token.set(publishToken)
    }
    pluginConfiguration {
        ideaVersion {
            // Let the Gradle plugin set the since-build version. It defaults to the version of the IDE we're building against
            // specified as two components, `{branch}.{build}` (e.g., "241.15989"). There is no third component specified.
            // The until-build version defaults to `{branch}.*`, but we want to support _all_ future versions, so we set it
            // with a null provider (the provider is important).
            // By letting the Gradle plugin handle this, the Plugin DevKit IntelliJ plugin cannot help us with the "Usage of
            // IntelliJ API not available in older IDEs" inspection. However, since our since-build is the version we compile
            // against, we can never get an API that's newer - it would be an unresolved symbol.
            untilBuild.set(provider { null })
        }
    }

}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    sourceSets.all {
        languageSettings.apply {
            languageVersion = "2.0"
        }
    }
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    wrapper {
        gradleVersion = gradleVersion
    }
}

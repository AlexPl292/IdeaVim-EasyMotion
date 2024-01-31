import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

/*
The settings script is an entry point for defining a TeamCity
project hierarchy. The script should contain a single call to the
project() function with a Project instance or an init function as
an argument.

VcsRoots, BuildTypes, Templates, and subprojects can be
registered inside the project using the vcsRoot(), buildType(),
template(), and subProject() methods respectively.

To debug settings scripts in command-line, run the

    mvnDebug org.jetbrains.teamcity:teamcity-configs-maven-plugin:generate

command and attach your debugger to the port 8000.

To debug in IntelliJ Idea, open the 'Maven Projects' tool window (View
-> Tool Windows -> Maven Projects), find the generate task node
(Plugins -> teamcity-configs -> teamcity-configs:generate), the
'Debug' option is available in the context menu for the task.
*/

version = "2023.11"

project {

    buildType(Build)
    buildType(Publish)

    features {
        feature {
            id = "PROJECT_EXT_242"
            type = "IssueTracker"
            param("secure:password", "")
            param("name", "AlexPl292/IdeaVim-EasyMotion")
            param("pattern", """#(\d+)""")
            param("authType", "anonymous")
            param("repository", "https://github.com/AlexPl292/IdeaVim-EasyMotion")
            param("type", "GithubIssues")
            param("secure:accessToken", "")
            param("username", "")
        }
    }
}

object Build : BuildType({
    name = "Build"

    params {
        param("env.ORG_GRADLE_PROJECT_aceJumpFromMarketplace", "true")
        param("env.ORG_GRADLE_PROJECT_ideaVimFromMarketplace", "true")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            tasks = "clean :test"
            buildFile = "build.gradle"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
    }

    triggers {
        vcs {
        }
    }

    failureConditions {
        javaCrash = false
    }
})

object Publish : BuildType({
    name = "Publish"

    params {
        param("env.ORG_GRADLE_PROJECT_aceJumpFromMarketplace", "true")
        param("env.ORG_GRADLE_PROJECT_ideaVimFromMarketplace", "true")
        password("env.ORG_GRADLE_PROJECT_publishToken", "credentialsJSON:71060840-8359-4975-bf7d-89c158f75c36", label = "Token", display = ParameterDisplay.HIDDEN)
        param("env.ORG_GRADLE_PROJECT_publishUsername", "Aleksei.Plate")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        gradle {
            tasks = "clean publishPlugin"
            buildFile = "build.gradle"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
    }

    failureConditions {
        javaCrash = false
    }
})

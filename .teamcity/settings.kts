import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.script
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.schedule
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

version = "2020.1"

project {

    buildType(Build)
    buildType(BuildMaster)
    buildType(Publish)
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
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
    }

    triggers {
        vcs {
        }
    }
})

object BuildMaster : BuildType({
    name = "Build Master"
    description = "Daily task that tests IdeaVim-EasyMotion against masters of IdeaVim and AceJump"

    params {
        param("env.ORG_GRADLE_PROJECT_aceJumpFromMarketplace", "false")
        param("env.ORG_GRADLE_PROJECT_ideaVimFromMarketplace", "false")
    }

    vcs {
        root(DslContext.settingsRoot)
    }

    steps {
        script {
            name = "Download masters"
            scriptContent = """
                echo "Clone AceJump"
                if [ -d "AceJump" ]; then rm -Rf AceJump; fi
                git clone https://github.com/acejump/AceJump.git
                
                echo "Clone IdeaVim"
                if [ -d "ideavim" ]; then rm -Rf ideavim; fi
                if [ -d "IdeaVIM" ]; then rm -Rf IdeaVIM; fi
                git clone https://github.com/JetBrains/ideavim IdeaVIM
                
                echo "Apply patch"
                git apply -- masters.patch
            """.trimIndent()
        }
        gradle {
            tasks = "clean :test -x patchPluginXml --scan"
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
    }

    triggers {
        schedule {
            enabled = false
            branchFilter = ""
            triggerBuild = always()
            withPendingChangesOnly = false
        }
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
            param("org.jfrog.artifactory.selectedDeployableServer.defaultModuleVersionConfiguration", "GLOBAL")
        }
    }
})

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.projectFeatures.dockerRegistry

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

version = "2025.11"

project {
    description = "C++17 calculator (lib + app)"

    params {
        param("repo.url", "https://github.com/WFHasaidi/tdg-devops-test")
        param("env.REGISTRY_HOST", "registry.bahamout.fr")
        param("env.TC_BUILD_DIR_NAME", "build-%env.TC_OS%-%env.TC_COMPILER%_%build.number%")
        param("system.teamcity.build.checkoutDir.expireHours", "0")
    }

    features {
        dockerRegistry {
            id = "PROJECT_EXT_4"
            name = "registry-bahamout"
            url = "https://registry.bahamout.fr"
            userName = "asaidi"
            password = "credentialsJSON:4884a009-a815-486f-9a1a-421b865bb7e6"
        }
    }

    vcsRoot(TdgVcsRoot)

    buildType(LinuxGCC)
    buildType(LinuxClangFixed)
    buildType(WindowsMSVCFixed)
    buildType(LinuxGCCFixedQuality)
    buildType(CrossPlatformFixed)
    buildType(NightlyFixedMatrix)
}

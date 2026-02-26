import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerRegistryConnections
import jetbrains.buildServer.configs.kotlin.buildFeatures.runInDocker
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object LinuxGCCRelease : BuildType({
    name = "CI :: Linux GCC :: Release"

    vcs {
        root(TdgVcsRoot)
        cleanCheckout = false
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
        exists("docker.server.version")
    }

    params {
        param("env.TC_OS", "Rocky9")
        param("env.TC_COMPILER", "gcc")
        param("env.CMAKE_CONFIGURE_PRESET_RELEASE", "release")
        param("env.CMAKE_BUILD_PRESET_RELEASE", "release")
        param("env.BUILD_JOBS", "8")
        param("env.DOCKER_IMAGE", "%env.REGISTRY_HOST%/tc-cpp-build:gcc")
    }

    features {
        dockerRegistryConnections {
            cleanupPushedImages = true
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_4"
            }
        }
        runInDocker {
            dockerImage = "%env.DOCKER_IMAGE%"
            dockerPull = true
        }
    }

    steps {
        script {
            name = "Configure + Build (Release)"
            scriptContent = """
                set -euo pipefail
                mkdir -p logs

                cmake --preset "%env.CMAKE_CONFIGURE_PRESET_RELEASE%"
                cmake --build --preset "%env.CMAKE_BUILD_PRESET_RELEASE%" --parallel "%env.BUILD_JOBS%"
            """.trimIndent()
        }

        script {
            name = "Publish artifacts (Release)"
            scriptContent = """
                set -euo pipefail

                export CCACHE_DIR="/var/ccache/agents/%teamcity.agent.name%/%system.teamcity.buildType.id%"
                export CCACHE_BASEDIR="/ws"
                export CCACHE_COMPRESS=1

                rm -rf artifacts/stage-release artifacts/full-build logs
                mkdir -p artifacts logs
                BUILD_DIR="out/build/%env.TC_UNIQUE_DIR%/release"
                cmake --install "${'$'}BUILD_DIR" --config Release --prefix artifacts/stage-release

                if [ "%env.PUBLISH_FULL_BUILD_DIR%" = "true" ] && [ -d "${'$'}BUILD_DIR" ]; then
                  cp -a "${'$'}BUILD_DIR" artifacts/full-build || true
                fi
                echo "Release staging artifacts prepared."
            """.trimIndent()
        }
    }

    artifactRules = """
        +:logs/** => logs
        +:artifacts/stage-release/** => stage-release
        +:artifacts/full-build/** => full-build
    """.trimIndent()
})

import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.XmlReport.XmlReportType
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerRegistryConnections
import jetbrains.buildServer.configs.kotlin.buildFeatures.runInDocker
import jetbrains.buildServer.configs.kotlin.buildFeatures.xmlReport
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object LinuxGCCDebug : BuildType({
    name = "CI :: Linux GCC :: Debug"

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
        param("env.CMAKE_CONFIGURE_PRESET_DEBUG", "default")
        param("env.CMAKE_BUILD_PRESET_DEBUG", "default")
        param("env.CMAKE_TEST_PRESET_DEBUG", "default")
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
        xmlReport {
            reportType = XmlReportType.JUNIT
            rules = "logs/ctest-junit.xml"
        }
    }

    steps {
        script {
            name = "Configure + Build + Test (Debug)"
            scriptContent = """
                set -euo pipefail
                rm -rf logs
                mkdir -p logs

                cmake --preset "%env.CMAKE_CONFIGURE_PRESET_DEBUG%"
                cmake --build --preset "%env.CMAKE_BUILD_PRESET_DEBUG%" --parallel "%env.BUILD_JOBS%"
                ctest \
                  --preset "%env.CMAKE_TEST_PRESET_DEBUG%" \
                  --test-dir "out/build/%env.TC_UNIQUE_DIR%/default" \
                  --output-on-failure \
                  --output-junit "$(pwd)/logs/ctest-junit.xml"
            """.trimIndent()
        }

        script {
            name = "Publish artifacts (Debug)"
            scriptContent = """
                set -euo pipefail

                export CCACHE_DIR="/var/ccache/agents/%teamcity.agent.name%/%system.teamcity.buildType.id%"
                export CCACHE_BASEDIR="/ws"
                export CCACHE_COMPRESS=1

                rm -rf artifacts/stage-debug artifacts/full-build
                mkdir -p artifacts
                BUILD_DIR="out/build/%env.TC_UNIQUE_DIR%/default"

                cmake --install "${'$'}BUILD_DIR" --config Debug --prefix artifacts/stage-debug

                if [ "%env.PUBLISH_FULL_BUILD_DIR%" = "true" ] && [ -d "${'$'}BUILD_DIR" ]; then
                  cp -a "${'$'}BUILD_DIR" artifacts/full-build || true
                fi
                echo "Debug staging artifacts prepared."
            """.trimIndent()
        }
    }

    artifactRules = """
        +:logs/** => logs
        +:artifacts/stage-debug/** => stage-debug
        +:artifacts/full-build/** => full-build
    """.trimIndent()
})

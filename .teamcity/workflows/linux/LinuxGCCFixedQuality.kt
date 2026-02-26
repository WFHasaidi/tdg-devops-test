import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.XmlReport.XmlReportType
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerRegistryConnections
import jetbrains.buildServer.configs.kotlin.buildFeatures.xmlReport
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object LinuxGCCFixedQuality : BuildType({
    id("LinuxGCCFixedQuality")
    name = "Nightly :: Linux GCC (fixed) :: Quality"

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
        param("env.CMAKE_CONFIGURE_PRESET_DEBUG", "fixed")
        param("env.CMAKE_BUILD_PRESET_DEBUG", "fixed")
        param("env.CMAKE_TEST_PRESET_DEBUG", "fixed")
        param("env.BUILD_JOBS", "8")
        param("env.PERF_REPEAT", "200")
        param("env.DOCKER_IMAGE", "%env.REGISTRY_HOST%/tc-cpp-build:gcc")
    }

    features {
        dockerRegistryConnections {
            cleanupPushedImages = true
            loginToRegistry = on {
                dockerRegistryId = "PROJECT_EXT_4"
            }
        }
        xmlReport {
            reportType = XmlReportType.JUNIT
            rules = "logs/ctest-junit.xml"
        }
    }

    steps {
        script {
            name = "Quality checks (Valgrind, gcovr, perf)"
            scriptContent = """
                set -euo pipefail

                HOST_UID="$(id -u)"
                HOST_GID="$(id -g)"

                docker run --rm -i \
                  --name "tc-%teamcity.build.id%-quality-gcc-fixed" \
                  --mount type=bind,src="${'$'}PWD",dst=/ws \
                  -w /ws \
                  --cap-add SYS_PTRACE \
                  --cap-add PERFMON \
                  --security-opt seccomp=unconfined \
                  -e CMAKE_CONFIGURE_PRESET="%env.CMAKE_CONFIGURE_PRESET_DEBUG%" \
                  -e CMAKE_BUILD_PRESET="%env.CMAKE_BUILD_PRESET_DEBUG%" \
                  -e TEST_PRESET="%env.CMAKE_TEST_PRESET_DEBUG%" \
                  -e BUILD_JOBS="%env.BUILD_JOBS%" \
                  -e TC_UNIQUE_DIR="%env.TC_UNIQUE_DIR%" \
                  -e BUILD_DIR="/ws/out/build/%env.TC_UNIQUE_DIR%/fixed" \
                  -e PERF_REPEAT="%env.PERF_REPEAT%" \
                  -e HOST_UID="${'$'}HOST_UID" \
                  -e HOST_GID="${'$'}HOST_GID" \
                  "%env.DOCKER_IMAGE%" \
                  bash /ws/ci/scripts/quality_linux_gcc_fixed.sh
            """.trimIndent()
        }
    }

    artifactRules = """
        +:logs/coverage* => reports
        +:logs/valgrind.html => reports
        +:logs/perf-stat.html => reports
        +:logs/perf-report.html => reports
        +:logs/flamegraph.svg => reports
        +:logs/valgrind.log => reports
        +:logs/perf-stat.txt => reports
        +:logs/perf-report.txt => reports
        +:logs/perf.data => reports
        +:logs/perf.folded => reports
        +:logs/perf.folded.filtered => reports
        +:logs/** => logs
    """.trimIndent()
})

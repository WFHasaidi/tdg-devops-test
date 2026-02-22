import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.XmlReport.XmlReportType
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerRegistryConnections
import jetbrains.buildServer.configs.kotlin.buildFeatures.xmlReport
import jetbrains.buildServer.configs.kotlin.buildSteps.script
import jetbrains.buildServer.configs.kotlin.triggers.vcs

object CiLinuxGccBuildOnly : BuildType({
    name = "CI :: Linux GCC (build-only)"

    // Use explicit VCS root from settings.kts to control branchSpec behavior.
    vcs {
        root(TdgVcsRoot)
        cleanCheckout = true
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
        exists("docker.server.version")
    }

    params {
        // Build identity for inherited TC_BUILD_DIR_NAME pattern
        param("env.TC_OS", "Rocky9")
        param("env.TC_COMPILER", "gcc")

        param("env.CMAKE_CONFIGURE_PRESET_DEBUG", "default")
        param("env.CMAKE_BUILD_PRESET_DEBUG", "default")
        param("env.CMAKE_TEST_PRESET_DEBUG", "default")
        param("env.CMAKE_CONFIGURE_PRESET_RELEASE", "release")
        param("env.CMAKE_BUILD_PRESET_RELEASE", "release")

        // Parallelism
        param("env.BUILD_JOBS", "8")

        // Docker image containing gcc/cmake/ninja
        param("env.DOCKER_IMAGE", "%env.REGISTRY_HOST%/bahamout/tc-cpp-build:gcc")
    }

    triggers {
        vcs {
            branchFilter = """
                +:*
                -:refs/tags/*
            """.trimIndent()
        }
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
            name = "Tool versions (CMake, GCC, Ninja)"
            scriptContent = """
                set -euo pipefail

                echo "Image: %env.DOCKER_IMAGE%"
                mkdir -p logs

                docker run --rm \
                  --name "tc-%teamcity.build.id%-tool-versions" \
                  --user "$(id -u):$(id -g)" \
                  --mount type=bind,src="${'$'}PWD",dst=/ws \
                  -w /ws \
                  -e CMAKE_CONFIGURE_PRESET="%env.CMAKE_CONFIGURE_PRESET_DEBUG%" \
                  -e CMAKE_BUILD_PRESET="%env.CMAKE_BUILD_PRESET_DEBUG%" \
                  -e CI_PHASE="tool-versions" \
                  "%env.DOCKER_IMAGE%" \
                  bash -lc '
                    set -euo pipefail
                    chmod +x ./scripts/ci_linux.sh || true
                    ./scripts/ci_linux.sh "${'$'}CMAKE_CONFIGURE_PRESET" "${'$'}CMAKE_BUILD_PRESET" \
                      > >(tee /ws/logs/tool-versions.out.log) \
                      2> >(tee /ws/logs/tool-versions.err.log >&2)
                  '
            """.trimIndent()
        }

        script {
            name = "Configure (Debug, Docker, GCC)"
            scriptContent = """
                set -euo pipefail

                echo "Configure preset: %env.CMAKE_CONFIGURE_PRESET_DEBUG%"
                mkdir -p logs

                docker run --rm \
                  --name "tc-%teamcity.build.id%-configure" \
                  --user "$(id -u):$(id -g)" \
                  --mount type=bind,src="${'$'}PWD",dst=/ws \
                  -w /ws \
                  -e CMAKE_CONFIGURE_PRESET="%env.CMAKE_CONFIGURE_PRESET_DEBUG%" \
                  -e CMAKE_BUILD_PRESET="%env.CMAKE_BUILD_PRESET_DEBUG%" \
                  -e CI_PHASE="configure" \
                  "%env.DOCKER_IMAGE%" \
                  bash -lc '
                    set -euo pipefail
                    chmod +x ./scripts/ci_linux.sh || true
                    ./scripts/ci_linux.sh "${'$'}CMAKE_CONFIGURE_PRESET" "${'$'}CMAKE_BUILD_PRESET" \
                      > >(tee /ws/logs/configure.out.log) \
                      2> >(tee /ws/logs/configure.err.log >&2)
                  '
            """.trimIndent()
        }

        script {
            name = "Build (Debug, Docker, GCC)"
            scriptContent = """
                set -euo pipefail

                echo "Build preset: %env.CMAKE_BUILD_PRESET_DEBUG%"
                echo "Build jobs: %env.BUILD_JOBS%"
                mkdir -p logs

                docker run --rm \
                  --name "tc-%teamcity.build.id%-build" \
                  --user "$(id -u):$(id -g)" \
                  --mount type=bind,src="${'$'}PWD",dst=/ws \
                  -w /ws \
                  -e CMAKE_CONFIGURE_PRESET="%env.CMAKE_CONFIGURE_PRESET_DEBUG%" \
                  -e CMAKE_BUILD_PRESET="%env.CMAKE_BUILD_PRESET_DEBUG%" \
                  -e BUILD_JOBS="%env.BUILD_JOBS%" \
                  -e CI_PHASE="build" \
                  "%env.DOCKER_IMAGE%" \
                  bash -lc '
                    set -euo pipefail
                    chmod +x ./scripts/ci_linux.sh || true
                    ./scripts/ci_linux.sh "${'$'}CMAKE_CONFIGURE_PRESET" "${'$'}CMAKE_BUILD_PRESET" \
                      > >(tee /ws/logs/build.out.log) \
                      2> >(tee /ws/logs/build.err.log >&2)
                  '
            """.trimIndent()
        }

        script {
            name = "Configure (Release, Docker, GCC)"
            scriptContent = """
                set -euo pipefail

                echo "Configure preset: %env.CMAKE_CONFIGURE_PRESET_RELEASE%"
                mkdir -p logs

                docker run --rm \
                  --name "tc-%teamcity.build.id%-configure-release" \
                  --user "$(id -u):$(id -g)" \
                  --mount type=bind,src="${'$'}PWD",dst=/ws \
                  -w /ws \
                  -e CMAKE_CONFIGURE_PRESET="%env.CMAKE_CONFIGURE_PRESET_RELEASE%" \
                  -e CMAKE_BUILD_PRESET="%env.CMAKE_BUILD_PRESET_RELEASE%" \
                  -e CI_PHASE="configure" \
                  "%env.DOCKER_IMAGE%" \
                  bash -lc '
                    set -euo pipefail
                    chmod +x ./scripts/ci_linux.sh || true
                    ./scripts/ci_linux.sh "${'$'}CMAKE_CONFIGURE_PRESET" "${'$'}CMAKE_BUILD_PRESET" \
                      > >(tee /ws/logs/configure-release.out.log) \
                      2> >(tee /ws/logs/configure-release.err.log >&2)
                  '
            """.trimIndent()
        }

        script {
            name = "Build (Release, Docker, GCC)"
            scriptContent = """
                set -euo pipefail

                echo "Build preset: %env.CMAKE_BUILD_PRESET_RELEASE%"
                echo "Build jobs: %env.BUILD_JOBS%"
                mkdir -p logs

                docker run --rm \
                  --name "tc-%teamcity.build.id%-build-release" \
                  --user "$(id -u):$(id -g)" \
                  --mount type=bind,src="${'$'}PWD",dst=/ws \
                  -w /ws \
                  -e CMAKE_CONFIGURE_PRESET="%env.CMAKE_CONFIGURE_PRESET_RELEASE%" \
                  -e CMAKE_BUILD_PRESET="%env.CMAKE_BUILD_PRESET_RELEASE%" \
                  -e BUILD_JOBS="%env.BUILD_JOBS%" \
                  -e CI_PHASE="build" \
                  "%env.DOCKER_IMAGE%" \
                  bash -lc '
                    set -euo pipefail
                    chmod +x ./scripts/ci_linux.sh || true
                    ./scripts/ci_linux.sh "${'$'}CMAKE_CONFIGURE_PRESET" "${'$'}CMAKE_BUILD_PRESET" \
                      > >(tee /ws/logs/build-release.out.log) \
                      2> >(tee /ws/logs/build-release.err.log >&2)
                  '
            """.trimIndent()
        }

        script {
            name = "Test (Debug, CTest + JUnit)"
            scriptContent = """
                set -euo pipefail

                echo "Test preset: %env.CMAKE_TEST_PRESET_DEBUG%"
                mkdir -p logs

                docker run --rm \
                  --name "tc-%teamcity.build.id%-test-debug" \
                  --user "$(id -u):$(id -g)" \
                  --mount type=bind,src="${'$'}PWD",dst=/ws \
                  -w /ws \
                  -e CMAKE_CONFIGURE_PRESET="%env.CMAKE_CONFIGURE_PRESET_DEBUG%" \
                  -e CMAKE_BUILD_PRESET="%env.CMAKE_BUILD_PRESET_DEBUG%" \
                  -e TEST_PRESET="%env.CMAKE_TEST_PRESET_DEBUG%" \
                  -e CI_PHASE="test" \
                  "%env.DOCKER_IMAGE%" \
                  bash -lc '
                    set -euo pipefail
                    chmod +x ./scripts/ci_linux.sh || true
                    ./scripts/ci_linux.sh "${'$'}CMAKE_CONFIGURE_PRESET" "${'$'}CMAKE_BUILD_PRESET" \
                      > >(tee /ws/logs/test-debug.out.log) \
                      2> >(tee /ws/logs/test-debug.err.log >&2)
                  '
            """.trimIndent()
        }

        script {
            name = "Publish artifacts"
            scriptContent = """
                set -euo pipefail

                mkdir -p artifacts
                if [ -d "build" ]; then
                  cp -a "build" artifacts/build-debug || true
                fi
                if [ -d "build-release" ]; then
                  cp -a "build-release" artifacts/build-release || true
                fi
                echo "Artifacts prepared."
            """.trimIndent()
        }
    }

    artifactRules = """
        +:logs/** => logs
        +:artifacts/build-debug/** => build-debug
        +:artifacts/build-release/** => build-release
    """.trimIndent()
})

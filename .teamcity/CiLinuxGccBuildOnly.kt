import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerRegistryConnections
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

        // CMake preset name
        param("env.CMAKE_CONFIGURE_PRESET", "default")
        param("env.CMAKE_BUILD_PRESET", "default")

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
                  -e CMAKE_CONFIGURE_PRESET="%env.CMAKE_CONFIGURE_PRESET%" \
                  -e CMAKE_BUILD_PRESET="%env.CMAKE_BUILD_PRESET%" \
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
            name = "Configure (Docker, GCC)"
            scriptContent = """
                set -euo pipefail

                echo "Preset: %env.CMAKE_CONFIGURE_PRESET%"
                mkdir -p logs

                docker run --rm \
                  --name "tc-%teamcity.build.id%-configure" \
                  --user "$(id -u):$(id -g)" \
                  --mount type=bind,src="${'$'}PWD",dst=/ws \
                  -w /ws \
                  -e CMAKE_CONFIGURE_PRESET="%env.CMAKE_CONFIGURE_PRESET%" \
                  -e CMAKE_BUILD_PRESET="%env.CMAKE_BUILD_PRESET%" \
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
            name = "Build (Docker, GCC)"
            scriptContent = """
                set -euo pipefail

                echo "Build preset: %env.CMAKE_BUILD_PRESET%"
                echo "Build jobs: %env.BUILD_JOBS%"
                mkdir -p logs

                docker run --rm \
                  --name "tc-%teamcity.build.id%-build" \
                  --user "$(id -u):$(id -g)" \
                  --mount type=bind,src="${'$'}PWD",dst=/ws \
                  -w /ws \
                  -e CMAKE_CONFIGURE_PRESET="%env.CMAKE_CONFIGURE_PRESET%" \
                  -e CMAKE_BUILD_PRESET="%env.CMAKE_BUILD_PRESET%" \
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
            name = "Publish artifacts"
            scriptContent = """
                set -euo pipefail

                mkdir -p artifacts
                if [ -d "build" ]; then
                  cp -a "build" artifacts/build-dir || true
                fi
                echo "Artifacts prepared."
            """.trimIndent()
        }
    }

    artifactRules = """
        +:logs/** => logs
        +:artifacts/build-dir/** => build-dir
    """.trimIndent()
})

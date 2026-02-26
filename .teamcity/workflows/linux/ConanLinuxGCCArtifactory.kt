import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.dockerRegistryConnections
import jetbrains.buildServer.configs.kotlin.buildFeatures.runInDocker
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object ConanLinuxGCCArtifactory : BuildType({
    name = "CI :: Conan :: Linux GCC -> Artifactory"

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
        // Per-agent Conan cache to reuse artifacts and avoid collisions
        param("env.CONAN_HOME", "/var/conan-cache/agents/%teamcity.agent.name%/gcc")
        param("env.DOCKER_IMAGE", "%env.REGISTRY_HOST%/tc-cpp-build:gcc")
        param("env.CONAN_PROFILE", "ci/conan/profiles/linux-gcc")
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
            name = "Create + Upload Conan package (Linux GCC)"
            scriptContent = """
                set -euo pipefail

                CONAN_REMOTE_PASSWORD="%CONAN_REMOTE_PASSWORD%"
                CONAN_REMOTE_URL="%env.CONAN_REMOTE_URL%"

                mkdir -p logs artifacts

                # Build-scoped paths (no intermediate variables)
                export CONAN_PACKAGE_VERSION="dev.%teamcity.build.id%"
                LOCKFILE="out/conan/locks/%teamcity.build.id%/linux-gcc.lock"
                mkdir -p "$(dirname "${'$'}LOCKFILE")"

                conan remote add artifactory "%env.CONAN_REMOTE_URL%" --force
                conan remote login artifactory admin -p "%CONAN_REMOTE_PASSWORD%"

                conan lock create conanfile.py \
                  -pr:h "%env.CONAN_PROFILE%" \
                  -pr:b "%env.CONAN_PROFILE%" \
                  -o "&:build_tests=False" \
                  -o "&:build_lib_only=True" \
                  --lockfile-out "${'$'}LOCKFILE"

                conan create . \
                  --version "${'$'}CONAN_PACKAGE_VERSION" \
                  --user "%env.CONAN_PKG_USER%" \
                  --channel "%env.CONAN_PKG_CHANNEL%" \
                  -pr:h "%env.CONAN_PROFILE%" \
                  -pr:b "%env.CONAN_PROFILE%" \
                  -o "&:build_tests=False" \
                  -o "&:build_lib_only=True" \
                  --lockfile "${'$'}LOCKFILE" \
                  --build=missing

                conan upload "calculator/${'$'}CONAN_PACKAGE_VERSION@%env.CONAN_PKG_USER%/%env.CONAN_PKG_CHANNEL%" -r artifactory --confirm

                conan list "calculator/*@%env.CONAN_PKG_USER%/%env.CONAN_PKG_CHANNEL%" -r artifactory > logs/conan-linux-gcc-remote-list.txt

                if [ -d out/conan/locks ]; then cp -a out/conan/locks artifacts/conan-locks; fi
                if [ -d out/conan/package-info ]; then cp -a out/conan/package-info artifacts/conan-package-info; fi
            """.trimIndent()
        }
    }

    artifactRules = """
        +:logs/** => logs
        +:artifacts/conan-locks/** => conan-locks
        +:artifacts/conan-package-info/** => conan-package-info
    """.trimIndent()
})

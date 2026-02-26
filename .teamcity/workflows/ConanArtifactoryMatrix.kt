import jetbrains.buildServer.configs.kotlin.*

object ConanArtifactoryMatrix : BuildType({
    name = "CI :: Conan Artifactory Matrix"
    type = BuildTypeSettings.Type.COMPOSITE
    description = "Create Conan packages on Linux GCC, Linux Clang and Windows MSVC, then upload them to Artifactory."

    vcs {
        root(TdgVcsRoot)
    }

    dependencies {
        dependency(ConanLinuxGCCArtifactory) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:logs/** => deps/linux-gcc/logs
                    +:conan-locks/** => deps/linux-gcc/conan-locks
                    +:conan-package-info/** => deps/linux-gcc/conan-package-info
                """.trimIndent()
            }
        }

        dependency(ConanLinuxClangArtifactory) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:logs/** => deps/linux-clang/logs
                    +:conan-locks/** => deps/linux-clang/conan-locks
                    +:conan-package-info/** => deps/linux-clang/conan-package-info
                """.trimIndent()
            }
        }

        dependency(ConanWindowsMSVCArtifactory) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:logs/** => deps/windows-msvc/logs
                    +:conan-locks/** => deps/windows-msvc/conan-locks
                    +:conan-package-info/** => deps/windows-msvc/conan-package-info
                """.trimIndent()
            }
        }
    }

    artifactRules = """
        +:deps/linux-gcc/** => conan/linux-gcc
        +:deps/linux-clang/** => conan/linux-clang
        +:deps/windows-msvc/** => conan/windows-msvc
    """.trimIndent()
})

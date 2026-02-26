import jetbrains.buildServer.configs.kotlin.*

object LinuxClangFixed : BuildType({
    name = "CI :: Linux Clang (fixed)"
    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        root(TdgVcsRoot)
    }

    dependencies {
        dependency(LinuxClangFixedDebug) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-debug/** => deps/linux-clang-fixed-debug
                    +:logs/** => deps/linux-clang-fixed-debug-logs
                """.trimIndent()
            }
        }
        dependency(LinuxClangFixedRelease) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-release/** => deps/linux-clang-fixed-release
                    +:logs/** => deps/linux-clang-fixed-release-logs
                """.trimIndent()
            }
        }
    }

    artifactRules = """
        +:deps/linux-clang-fixed-debug/** => stage-debug
        +:deps/linux-clang-fixed-release/** => stage-release
        +:deps/linux-clang-fixed-debug-logs/** => logs/debug
        +:deps/linux-clang-fixed-release-logs/** => logs/release
    """.trimIndent()
})

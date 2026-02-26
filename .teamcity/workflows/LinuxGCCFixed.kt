import jetbrains.buildServer.configs.kotlin.*

object LinuxGCCFixed : BuildType({
    name = "CI :: Linux GCC (fixed)"
    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        root(TdgVcsRoot)
    }

    dependencies {
        dependency(LinuxGCCFixedDebug) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-debug/** => deps/linux-gcc-fixed-debug
                    +:logs/** => deps/linux-gcc-fixed-debug-logs
                """.trimIndent()
            }
        }
        dependency(LinuxGCCFixedRelease) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-release/** => deps/linux-gcc-fixed-release
                    +:logs/** => deps/linux-gcc-fixed-release-logs
                """.trimIndent()
            }
        }
    }

    artifactRules = """
        +:deps/linux-gcc-fixed-debug/** => stage-debug
        +:deps/linux-gcc-fixed-release/** => stage-release
        +:deps/linux-gcc-fixed-debug-logs/** => logs/debug
        +:deps/linux-gcc-fixed-release-logs/** => logs/release
    """.trimIndent()
})

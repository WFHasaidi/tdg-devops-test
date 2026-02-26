import jetbrains.buildServer.configs.kotlin.*

object LinuxGCC : BuildType({
    name = "CI :: Linux GCC"
    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        root(TdgVcsRoot)
    }

    dependencies {
        dependency(LinuxGCCDebug) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-debug/** => deps/linux-gcc-debug
                    +:logs/** => deps/linux-gcc-debug-logs
                """.trimIndent()
            }
        }
        dependency(LinuxGCCRelease) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-release/** => deps/linux-gcc-release
                    +:logs/** => deps/linux-gcc-release-logs
                """.trimIndent()
            }
        }
    }

    artifactRules = """
        +:deps/linux-gcc-debug/** => stage-debug
        +:deps/linux-gcc-release/** => stage-release
        +:deps/linux-gcc-debug-logs/** => logs/debug
        +:deps/linux-gcc-release-logs/** => logs/release
    """.trimIndent()
})

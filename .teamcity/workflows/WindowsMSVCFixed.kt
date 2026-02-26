import jetbrains.buildServer.configs.kotlin.*

object WindowsMSVCFixed : BuildType({
    name = "CI :: Windows MSVC (fixed)"
    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        root(TdgVcsRoot)
    }

    dependencies {
        dependency(WindowsMSVCFixedDebug) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-debug/** => deps/windows-msvc-debug
                    +:logs/** => deps/windows-msvc-debug-logs
                """.trimIndent()
            }
        }
        dependency(WindowsMSVCFixedRelease) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-release/** => deps/windows-msvc-release
                    +:logs/** => deps/windows-msvc-release-logs
                """.trimIndent()
            }
        }
    }

    artifactRules = """
        +:deps/windows-msvc-debug/** => stage-debug
        +:deps/windows-msvc-release/** => stage-release
        +:deps/windows-msvc-debug-logs/** => logs/debug
        +:deps/windows-msvc-release-logs/** => logs/release
    """.trimIndent()
})

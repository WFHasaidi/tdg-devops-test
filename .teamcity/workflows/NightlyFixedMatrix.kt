import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.triggers.schedule

object NightlyFixedMatrix : BuildType({
    name = "Nightly :: Fixed Matrix"
    type = BuildTypeSettings.Type.COMPOSITE
    description = "Nightly/manual fixed validation across Linux GCC, Linux Clang, Windows MSVC, plus Linux quality checks."

    vcs {
        root(TdgVcsRoot)
    }

    triggers {
        schedule {
            schedulingPolicy = daily {
                hour = 0
                minute = 0
            }
            triggerBuild = always()
            withPendingChangesOnly = false
            branchFilter = """
                +:refs/heads/main
            """.trimIndent()
        }
    }

    dependencies {
        dependency(LinuxGCCFixed) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-debug/** => deps/linux-gcc-fixed-debug
                    +:stage-release/** => deps/linux-gcc-fixed-release
                    +:logs/** => deps/linux-gcc-fixed-logs
                """.trimIndent()
            }
        }

        dependency(LinuxClangFixed) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-debug/** => deps/linux-clang-fixed-debug
                    +:stage-release/** => deps/linux-clang-fixed-release
                    +:logs/** => deps/linux-clang-fixed-logs
                """.trimIndent()
            }
        }

        dependency(WindowsMSVCFixed) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:stage-debug/** => deps/windows-fixed-debug
                    +:stage-release/** => deps/windows-fixed-release
                    +:logs/** => deps/windows-fixed-logs
                """.trimIndent()
            }
        }

        dependency(LinuxGCCFixedQuality) {
            snapshot {
                synchronizeRevisions = true
                runOnSameAgent = false
                onDependencyFailure = FailureAction.FAIL_TO_START
                onDependencyCancel = FailureAction.CANCEL
            }
            artifacts {
                artifactRules = """
                    +:reports/** => deps/linux-quality-reports
                    +:quality/** => deps/linux-quality
                    +:logs/** => deps/linux-quality-logs
                """.trimIndent()
            }
        }
    }

    artifactRules = """
        +:deps/linux-gcc-fixed-debug/** => stage/linux-gcc/debug
        +:deps/linux-gcc-fixed-release/** => stage/linux-gcc/release
        +:deps/linux-clang-fixed-debug/** => stage/linux-clang/debug
        +:deps/linux-clang-fixed-release/** => stage/linux-clang/release
        +:deps/windows-fixed-debug/** => stage/windows-msvc/debug
        +:deps/windows-fixed-release/** => stage/windows-msvc/release
        +:deps/linux-quality-reports/** => quality/linux-gcc/reports
        +:deps/linux-quality/** => quality/linux-gcc
        +:deps/linux-gcc-fixed-logs/** => logs/linux-gcc
        +:deps/linux-clang-fixed-logs/** => logs/linux-clang
        +:deps/windows-fixed-logs/** => logs/windows-msvc
        +:deps/linux-quality-logs/** => logs/linux-quality
    """.trimIndent()
})

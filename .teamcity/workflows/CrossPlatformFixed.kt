import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.triggers.vcs

object CrossPlatformFixed : BuildType({
    name = "CI :: Cross-Platform (fixed)"
    type = BuildTypeSettings.Type.COMPOSITE

    vcs {
        root(TdgVcsRoot)
    }

    features {
        commitStatusPublisher {
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "%github.token%"
                }
            }
        }
    }

    triggers {
        vcs {
            branchFilter = """
                +:*
                -:refs/tags/*
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
                    +:stage-debug/** => deps/linux-fixed-debug
                    +:stage-release/** => deps/linux-fixed-release
                    +:logs/** => deps/linux-fixed-logs
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
    }

    artifactRules = """
        +:deps/linux-fixed-debug/** => stage/linux/debug
        +:deps/linux-fixed-release/** => stage/linux/release
        +:deps/windows-fixed-debug/** => stage/windows/debug
        +:deps/windows-fixed-release/** => stage/windows/release
        +:deps/linux-fixed-logs/** => logs/linux
        +:deps/windows-fixed-logs/** => logs/windows
    """.trimIndent()
})

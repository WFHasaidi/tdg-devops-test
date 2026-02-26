import jetbrains.buildServer.configs.kotlin.*

object CiWorkflows : Project({
    name = "CI :: Workflows"
    description = "Entry-point CI workflows for baseline, fixed, and cross-platform validation."

    buildType(LinuxGCC)
    buildType(LinuxGCCFixed)
    buildType(LinuxClangFixed)
    buildType(WindowsMSVCFixed)
    buildType(CrossPlatformFixed)
    buildType(NightlyFixedMatrix)
    buildType(ConanArtifactoryMatrix)
})

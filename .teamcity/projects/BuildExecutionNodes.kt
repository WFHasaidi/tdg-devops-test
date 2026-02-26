import jetbrains.buildServer.configs.kotlin.*

object BuildExecutionNodes : Project({
    name = "CI :: Build Execution Nodes"
    description = "Debug/Release execution jobs used by CI workflow composites."

    buildType(LinuxGCCDebug)
    buildType(LinuxGCCRelease)
    buildType(LinuxGCCFixedDebug)
    buildType(LinuxGCCFixedRelease)
    buildType(LinuxGCCFixedQuality)
    buildType(LinuxClangFixedDebug)
    buildType(LinuxClangFixedRelease)
    buildType(WindowsMSVCFixedDebug)
    buildType(WindowsMSVCFixedRelease)
    buildType(ConanLinuxGCCArtifactory)
    buildType(ConanLinuxClangArtifactory)
    buildType(ConanWindowsMSVCArtifactory)
})

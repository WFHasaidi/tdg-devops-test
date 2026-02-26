import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object WindowsMSVCFixedRelease : BuildType({
    name = "CI :: Windows MSVC (fixed) :: Release"

    vcs {
        root(TdgVcsRoot)
        cleanCheckout = false
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Windows")
    }

    params {
        param("env.TC_OS", "Windows")
        param("env.TC_COMPILER", "msvc")
        param("env.CMAKE_CONFIGURE_PRESET_RELEASE", "fixed-release")
        param("env.CMAKE_BUILD_PRESET_RELEASE", "fixed-release")
        param("env.BUILD_JOBS", "8")
        param("env.TC_UNIQUE_DIR", "%teamcity.build.id%")
        param("TC_UNIQUE_DIR", "%teamcity.build.id%")
    }

    steps {
        script {
            name = "Configure + Build (Release, MSVC)"
            scriptContent = """
                @echo off
                setlocal EnableExtensions EnableDelayedExpansion

                if "%env.TC_UNIQUE_DIR%"=="" set "TC_UNIQUE_DIR=local" else set "TC_UNIQUE_DIR=%env.TC_UNIQUE_DIR%"

                if not exist logs mkdir logs

                call "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat" -arch=amd64 -host_arch=amd64 || exit /b 1

                cmake --version || exit /b 1
                ninja --version || exit /b 1

                cmake --preset %env.CMAKE_CONFIGURE_PRESET_RELEASE% -DTC_UNIQUE_DIR=%TC_UNIQUE_DIR% || exit /b 1
                cmake --build --preset %env.CMAKE_BUILD_PRESET_RELEASE% --parallel %env.BUILD_JOBS% || exit /b 1

                if not exist artifacts mkdir artifacts
                set "BUILD_DIR=out/build/%TC_UNIQUE_DIR%/fixed-release"
                cmake --install "!BUILD_DIR!" --config Release --prefix artifacts\stage-release || exit /b 1
                if /I "%env.PUBLISH_FULL_BUILD_DIR%"=="true" (
                  if exist "!BUILD_DIR!" xcopy /E /I /Y "!BUILD_DIR!" artifacts\full-build >nul
                )
            """.trimIndent()
        }
    }

    artifactRules = """
        +:logs/** => logs
        +:artifacts/stage-release/** => stage-release
        +:artifacts/full-build/** => full-build
    """.trimIndent()
})

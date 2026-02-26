import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildFeatures.XmlReport.XmlReportType
import jetbrains.buildServer.configs.kotlin.buildFeatures.xmlReport
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object WindowsMSVCFixedDebug : BuildType({
    name = "CI :: Windows MSVC (fixed) :: Debug"

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
        param("env.CMAKE_CONFIGURE_PRESET_DEBUG", "fixed")
        param("env.CMAKE_BUILD_PRESET_DEBUG", "fixed")
        param("env.CMAKE_TEST_PRESET_DEBUG", "fixed")
        param("env.BUILD_JOBS", "8")
        param("env.TC_UNIQUE_DIR", "%teamcity.build.id%")
        // Also set as a configuration parameter so TeamCity implicit requirements are satisfied.
        param("TC_UNIQUE_DIR", "%teamcity.build.id%")
    }

    features {
        xmlReport {
            reportType = XmlReportType.JUNIT
            rules = "logs/ctest-junit-windows.xml"
        }
    }

    steps {
        script {
            name = "Configure + Build + Test (Debug, MSVC)"
            scriptContent = """
                @echo off
                setlocal EnableExtensions EnableDelayedExpansion

                if "%env.TC_UNIQUE_DIR%"=="" set "TC_UNIQUE_DIR=local" else set "TC_UNIQUE_DIR=%env.TC_UNIQUE_DIR%"

                if not exist logs mkdir logs

                call "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat" -arch=amd64 -host_arch=amd64 || exit /b 1

                cmake --version || exit /b 1
                ninja --version || exit /b 1

                cmake --preset %env.CMAKE_CONFIGURE_PRESET_DEBUG% -DTC_UNIQUE_DIR=%TC_UNIQUE_DIR% || exit /b 1
                cmake --build --preset %env.CMAKE_BUILD_PRESET_DEBUG% --parallel %env.BUILD_JOBS% || exit /b 1
                ctest --preset %env.CMAKE_TEST_PRESET_DEBUG% --output-on-failure --output-junit logs\ctest-junit-windows.xml || exit /b 1

                if not exist artifacts mkdir artifacts
                set "BUILD_DIR=out/build/%TC_UNIQUE_DIR%/fixed"
                cmake --install "!BUILD_DIR!" --config Debug --prefix artifacts\stage-debug || exit /b 1
                if /I "%env.PUBLISH_FULL_BUILD_DIR%"=="true" (
                  if exist "!BUILD_DIR!" xcopy /E /I /Y "!BUILD_DIR!" artifacts\full-build >nul
                )
            """.trimIndent()
        }
    }

    artifactRules = """
        +:logs/** => logs
        +:artifacts/stage-debug/** => stage-debug
        +:artifacts/full-build/** => full-build
    """.trimIndent()
})

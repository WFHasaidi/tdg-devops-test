import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.buildSteps.script

object ConanWindowsMSVCArtifactory : BuildType({
    name = "CI :: Conan :: Windows MSVC -> Artifactory"

    vcs {
        root(TdgVcsRoot)
        cleanCheckout = false
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Windows")
    }

    params {
        param("CONAN_PKG_USER", "%env.CONAN_PKG_USER%")
        param("CONAN_PKG_CHANNEL", "%env.CONAN_PKG_CHANNEL%")
        param("env.CONAN_PROFILE", "ci/conan/profiles/windows-msvc")
        param("env.CONAN_HOME", "C:\\conan-cache\\agents\\%teamcity.agent.name%\\msvc")
    }

    steps {
        script {
            name = "Create + Upload Conan package (Windows MSVC)"
            scriptContent = """
                @echo off
                setlocal EnableExtensions EnableDelayedExpansion

                if not exist logs mkdir logs
                if not exist artifacts mkdir artifacts

                call "C:\Program Files (x86)\Microsoft Visual Studio\2022\BuildTools\Common7\Tools\VsDevCmd.bat" -arch=amd64 -host_arch=amd64 || exit /b 1

                conan --version || exit /b 1

                set "CONAN_HOME=%env.CONAN_HOME%"
                set "CONAN_REMOTE_NAME=artifactory"
                set "CONAN_REMOTE_URL=%env.CONAN_REMOTE_URL%"
                set "CONAN_REMOTE_PASSWORD=%CONAN_REMOTE_PASSWORD%"
                set "CONAN_PKG_USER=%CONAN_PKG_USER%"
                set "CONAN_PKG_CHANNEL=%CONAN_PKG_CHANNEL%"
                set "CONAN_PACKAGE_VERSION=dev.%teamcity.build.id%"

                if not exist out\conan\locks\%teamcity.build.id% mkdir out\conan\locks\%teamcity.build.id%
                set "LOCKFILE=out\conan\locks\%teamcity.build.id%\windows-msvc.lock"

                conan remote add "!CONAN_REMOTE_NAME!" "!CONAN_REMOTE_URL!" --force
                conan remote login "!CONAN_REMOTE_NAME!" admin -p "!CONAN_REMOTE_PASSWORD!"

                conan lock create conanfile.py ^
                  -pr:h "%env.CONAN_PROFILE%" ^
                  -pr:b "%env.CONAN_PROFILE%" ^
                  -o "&:build_tests=False" ^
                  -o "&:build_lib_only=True" ^
                  --lockfile-out "!LOCKFILE!" || exit /b 1

                conan create . ^
                  --version "!CONAN_PACKAGE_VERSION!" ^
                  --user "!CONAN_PKG_USER!" ^
                  --channel "!CONAN_PKG_CHANNEL!" ^
                  -pr:h "%env.CONAN_PROFILE%" ^
                  -pr:b "%env.CONAN_PROFILE%" ^
                  -o "&:build_tests=False" ^
                  -o "&:build_lib_only=True" ^
                  --lockfile "!LOCKFILE!" ^
                  --build=missing || exit /b 1

                conan upload "calculator/!CONAN_PACKAGE_VERSION!@!CONAN_PKG_USER!/!CONAN_PKG_CHANNEL!" -r "!CONAN_REMOTE_NAME!" --confirm || exit /b 1

                conan list "calculator/*@!CONAN_PKG_USER!/!CONAN_PKG_CHANNEL!" -r "!CONAN_REMOTE_NAME!" > logs\conan-windows-msvc-remote-list.txt 2>&1

                if exist out\conan\locks xcopy /E /I /Y out\conan\locks artifacts\conan-locks >nul
                if exist out\conan\package-info xcopy /E /I /Y out\conan\package-info artifacts\conan-package-info >nul
            """.trimIndent()
        }
    }

    artifactRules = """
        +:logs/** => logs
        +:artifacts/conan-locks/** => conan-locks
        +:artifacts/conan-package-info/** => conan-package-info
    """.trimIndent()
})

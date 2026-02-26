import jetbrains.buildServer.configs.kotlin.*
import jetbrains.buildServer.configs.kotlin.projectFeatures.buildReportTab
import jetbrains.buildServer.configs.kotlin.projectFeatures.dockerRegistry



version = "2025.11"

project {
    description = "C++17 calculator (lib + app)"

    params {
        param("repo.url", "https://github.com/WFHasaidi/tdg-devops-test.git")
        param("env.REGISTRY_HOST", "registry.bahamout.fr")
        param("env.TC_UNIQUE_DIR", "%teamcity.build.id%")
        param("system.teamcity.build.checkoutDir.expireHours", "default")
        password("CONAN_REMOTE_PASSWORD", "credentialsJSON:d671d816-a36f-4b5e-8349-d6e0e530cf43")
        password("github.token", "credentialsJSON:22970b48-331b-4543-9a46-825891e2a99c")
        param("env.CONAN_REMOTE_URL", "https://artifactory.bahamout.fr/artifactory/api/conan/conan-local")
        param("env.CONAN_REMOTE_PASSWORD", "%CONAN_REMOTE_PASSWORD%")
        param("env.CONAN_PKG_USER", "admin")
        param("env.CONAN_PKG_CHANNEL", "stable")
        param("env.PUBLISH_FULL_BUILD_DIR", "false")
    }

    features {
        dockerRegistry {
            id = "PROJECT_EXT_4"
            name = "registry-bahamout"
            url = "https://registry.bahamout.fr"
            userName = "asaidi"
            password = "credentialsJSON:4884a009-a815-486f-9a1a-421b865bb7e6"
        }

        buildReportTab {
            id = "PROJECT_EXT_QUALITY_COVERAGE_TAB"
            title = "Coverage (gcovr)"
            startPage = "reports/coverage.html"
        }

        buildReportTab {
            id = "PROJECT_EXT_QUALITY_VALGRIND_TAB"
            title = "Valgrind"
            startPage = "reports/valgrind.html"
        }

        buildReportTab {
            id = "PROJECT_EXT_QUALITY_PERF_TAB"
            title = "Perf"
            startPage = "reports/perf-stat.html"
        }

        buildReportTab {
            id = "PROJECT_EXT_QUALITY_FLAMEGRAPH_TAB"
            title = "Flamegraph"
            startPage = "reports/flamegraph.svg"
        }

        buildReportTab {
            id = "PROJECT_EXT_MATRIX_COVERAGE_TAB"
            title = "Coverage (Nightly Matrix)"
            startPage = "quality/linux-gcc/reports/coverage.html"
        }

        buildReportTab {
            id = "PROJECT_EXT_MATRIX_VALGRIND_TAB"
            title = "Valgrind (Nightly Matrix)"
            startPage = "quality/linux-gcc/reports/valgrind.html"
        }

        buildReportTab {
            id = "PROJECT_EXT_MATRIX_PERF_TAB"
            title = "Perf (Nightly Matrix)"
            startPage = "quality/linux-gcc/reports/perf-stat.html"
        }

        buildReportTab {
            id = "PROJECT_EXT_MATRIX_FLAMEGRAPH_TAB"
            title = "Flamegraph (Nightly Matrix)"
            startPage = "quality/linux-gcc/reports/flamegraph.svg"
        }

        projectCustomChart {
            id = "PROJECT_EXT_QUALITY_COVERAGE_CHART"
            title = "Nightly Coverage (Linux GCC fixed)"
            seriesTitle = "Coverage %"
            format = CustomChart.Format.PERCENT
            series = listOf(
                CustomChart.Serie(
                    title = "Lines",
                    key = CustomChart.SeriesKey("coverage.lines.pct"),
                    sourceBuildTypeId = "LinuxGCCFixedQuality"
                ),
                CustomChart.Serie(
                    title = "Branches",
                    key = CustomChart.SeriesKey("coverage.branches.pct"),
                    sourceBuildTypeId = "LinuxGCCFixedQuality"
                ),
                CustomChart.Serie(
                    title = "Functions",
                    key = CustomChart.SeriesKey("coverage.functions.pct"),
                    sourceBuildTypeId = "LinuxGCCFixedQuality"
                )
            )
        }
    }

    vcsRoot(TdgVcsRoot)

    subProject(CiWorkflows)
    subProject(BuildExecutionNodes)
}

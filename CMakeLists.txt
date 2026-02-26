cmake_minimum_required(VERSION 3.15)
project(tdg-devops-test
    VERSION 1.0.0
    LANGUAGES CXX
    DESCRIPTION "TDG DevOps Technical Test Project"
)

include(GNUInstallDirs)
include(CMakePackageConfigHelpers)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# Export compile_commands.json for IDE support
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

# Build only the library/tests and skip the CLI app target when enabled.
option(BUILD_LIB_ONLY "Build only library targets (skip app executable)" OFF)

add_subdirectory(src/lib)
if(NOT BUILD_LIB_ONLY)
    add_subdirectory(src/app)
endif()

# Testing
option(BUILD_TESTS "Build unit tests" ON)
if(BUILD_TESTS)
    enable_testing()
    add_subdirectory(tests)
endif()

set(TDG_CMAKE_INSTALL_DIR "${CMAKE_INSTALL_LIBDIR}/cmake/calculator")

configure_package_config_file(
    "${CMAKE_CURRENT_SOURCE_DIR}/cmake/calculatorConfig.cmake.in"
    "${CMAKE_CURRENT_BINARY_DIR}/calculatorConfig.cmake"
    INSTALL_DESTINATION "${TDG_CMAKE_INSTALL_DIR}"
)

write_basic_package_version_file(
    "${CMAKE_CURRENT_BINARY_DIR}/calculatorConfigVersion.cmake"
    VERSION "${PROJECT_VERSION}"
    COMPATIBILITY SameMajorVersion
)

install(EXPORT calculatorTargets
    FILE calculatorTargets.cmake
    NAMESPACE calculator::
    DESTINATION "${TDG_CMAKE_INSTALL_DIR}"
)

install(FILES
    "${CMAKE_CURRENT_BINARY_DIR}/calculatorConfig.cmake"
    "${CMAKE_CURRENT_BINARY_DIR}/calculatorConfigVersion.cmake"
    DESTINATION "${TDG_CMAKE_INSTALL_DIR}"
)

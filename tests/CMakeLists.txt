find_package(GTest CONFIG QUIET
    NO_SYSTEM_ENVIRONMENT_PATH
    NO_CMAKE_SYSTEM_PATH
    NO_CMAKE_PACKAGE_REGISTRY
    NO_CMAKE_SYSTEM_PACKAGE_REGISTRY
)

if(NOT GTest_FOUND)
    include(FetchContent)

    # Do not install googletest/gmock when running project install.
    set(INSTALL_GTEST OFF CACHE BOOL "" FORCE)
    set(INSTALL_GMOCK OFF CACHE BOOL "" FORCE)
    set(BUILD_GMOCK OFF CACHE BOOL "" FORCE)

    FetchContent_Declare(
        googletest
        GIT_REPOSITORY https://github.com/google/googletest.git
        GIT_TAG        v1.14.0
    )

    # Prevent GoogleTest from overriding compiler/linker options on Windows
    set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)

    FetchContent_MakeAvailable(googletest)
endif()

add_executable(calculator_tests
    calculator_test.cpp
)

target_link_libraries(calculator_tests
    PRIVATE
        calculator
        GTest::gtest_main
)

include(GoogleTest)
gtest_discover_tests(calculator_tests)

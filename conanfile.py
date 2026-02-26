import os

from conan import ConanFile
from conan.tools.build import can_run
from conan.tools.cmake import CMakeDeps, CMakeToolchain


class TdgDevopsTestConan(ConanFile):
    name = "calculator"
    package_type = "static-library"
    license = "MIT"
    url = "https://github.com/WFHasaidi/tdg-devops-test"
    description = "C++ calculator library"

    settings = "os", "arch", "compiler", "build_type"
    exports_sources = (
        "CMakeLists.txt",
        "cmake/*",
        "src/*",
        "tests/*",
    )

    options = {
        "build_tests": [True, False],
        "build_lib_only": [True, False],
    }
    default_options = {
        "build_tests": False,
        "build_lib_only": True,
    }

    def package_id(self):
        # These options impact CI behavior, not the published ABI.
        del self.info.options.build_tests
        del self.info.options.build_lib_only

    def build_requirements(self):
        if self.options.build_tests:
            self.test_requires("gtest/1.14.0")

    def generate(self):
        deps = CMakeDeps(self)
        deps.generate()

        tc = CMakeToolchain(self)
        tc.cache_variables["BUILD_SHARED_LIBS"] = "OFF"
        tc.cache_variables["BUILD_TESTS"] = "ON" if self.options.build_tests else "OFF"
        tc.cache_variables["BUILD_LIB_ONLY"] = "ON" if self.options.build_lib_only else "OFF"
        # Keep existing repository CMakePresets.json untouched.
        tc.user_presets_path = False
        tc.generate()

    def build(self):
        generator = self.conf.get("tools.cmake.cmaketoolchain:generator", default="Ninja")
        build_type = str(self.settings.build_type)
        toolchain = os.path.join(self.generators_folder, "conan_toolchain.cmake")
        build_dir = os.path.join(self.build_folder, "cmake-build")

        self.run(
            f'cmake -S "{self.source_folder}" -B "{build_dir}" '
            f'-G "{generator}" '
            f'-DCMAKE_TOOLCHAIN_FILE="{toolchain}" '
            f'-DCMAKE_BUILD_TYPE="{build_type}" '
            f'-DBUILD_SHARED_LIBS=OFF '
            f'-DBUILD_TESTS={"ON" if self.options.build_tests else "OFF"} '
            f'-DBUILD_LIB_ONLY={"ON" if self.options.build_lib_only else "OFF"}'
        )
        self.run(f'cmake --build "{build_dir}" --config "{build_type}"')

    def test(self):
        if self.options.build_tests and can_run(self):
            build_type = str(self.settings.build_type)
            build_dir = os.path.join(self.build_folder, "cmake-build")
            self.run(f'ctest --test-dir "{build_dir}" --output-on-failure -C "{build_type}"')

    def package(self):
        build_type = str(self.settings.build_type)
        build_dir = os.path.join(self.build_folder, "cmake-build")
        self.run(
            f'cmake --install "{build_dir}" --config "{build_type}" --prefix "{self.package_folder}"'
        )

    def package_info(self):
        self.cpp_info.libs = ["calculator"]
        self.cpp_info.set_property("cmake_file_name", "calculator")
        self.cpp_info.set_property("cmake_target_name", "calculator::calculator")
        self.cpp_info.set_property("cmake_find_mode", "both")

#!/usr/bin/env bash
set -euo pipefail

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "ERROR: missing command: $1" >&2
    exit 1
  }
}

CONFIGURE_PRESET="${1:-default}"
BUILD_PRESET="${2:-$CONFIGURE_PRESET}"
BUILD_JOBS="${BUILD_JOBS:-8}"
CI_PHASE="${CI_PHASE:-all}"

echo "CONFIGURE_PRESET=${CONFIGURE_PRESET}"
echo "BUILD_PRESET=${BUILD_PRESET}"
echo "BUILD_JOBS=${BUILD_JOBS}"
echo "CI_PHASE=${CI_PHASE}"
echo "TC_BUILD_DIR_NAME=${TC_BUILD_DIR_NAME:-unset}"

require_cmd cmake
require_cmd ninja
require_cmd gcc

run_tool_versions() {
  cmake --version
  gcc --version
  ninja --version
}

run_configure() {
  cmake --preset "${CONFIGURE_PRESET}"
}

run_build() {
  cmake --build --preset "${BUILD_PRESET}" --parallel "${BUILD_JOBS}"
}

case "${CI_PHASE}" in
  tool-versions)
    run_tool_versions
    ;;
  configure)
    run_configure
    ;;
  build)
    run_build
    ;;
  all)
    run_tool_versions
    run_configure
    run_build
    ;;
  *)
    echo "ERROR: unsupported CI_PHASE '${CI_PHASE}'" >&2
    exit 2
    ;;
esac

echo "CI linux script completed."

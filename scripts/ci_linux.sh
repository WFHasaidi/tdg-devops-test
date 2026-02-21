#!/usr/bin/env bash
set -euo pipefail

tc_progress_start() { echo "##teamcity[progressStart '$1']" || true; }
tc_progress_finish() { echo "##teamcity[progressFinish '$1']" || true; }
tc_block_open() { echo "##teamcity[blockOpened name='$1']" || true; }
tc_block_close() { echo "##teamcity[blockClosed name='$1']" || true; }

require_cmd() {
  command -v "$1" >/dev/null 2>&1 || {
    echo "ERROR: missing command: $1" >&2
    exit 1
  }
}

CONFIGURE_PRESET="${1:-default}"
BUILD_PRESET="${2:-$CONFIGURE_PRESET}"
BUILD_JOBS="${BUILD_JOBS:-8}"

echo "CONFIGURE_PRESET=${CONFIGURE_PRESET}"
echo "BUILD_PRESET=${BUILD_PRESET}"
echo "BUILD_JOBS=${BUILD_JOBS}"
echo "TC_BUILD_DIR_NAME=${TC_BUILD_DIR_NAME:-unset}"

require_cmd cmake
require_cmd ninja

tc_block_open "Tool versions"
cmake --version
ninja --version
tc_block_close "Tool versions"

tc_progress_start "Configure"
cmake --preset "${CONFIGURE_PRESET}"
tc_progress_finish "Configure"

tc_progress_start "Build"
cmake --build --preset "${BUILD_PRESET}" --parallel "${BUILD_JOBS}"
tc_progress_finish "Build"

echo "CI linux script completed."

#!/usr/bin/env bash
set -euo pipefail

LOG_DIR="/ws/logs"
BUILD_DIR="${BUILD_DIR:-/ws/out/build/${TC_UNIQUE_DIR:-local}/fixed}"
TEST_BIN="${BUILD_DIR}/tests/calculator_tests"
FLAMEGRAPH_DIR="/opt/FlameGraph"

mkdir -p "${LOG_DIR}"

cmake --preset "${CMAKE_CONFIGURE_PRESET}" \
  -DCMAKE_C_FLAGS="--coverage -fno-omit-frame-pointer" \
  -DCMAKE_CXX_FLAGS="-DFIX_FACTORIAL_BUG --coverage -fno-omit-frame-pointer" \
  > "${LOG_DIR}/quality-configure.out.log" 2> "${LOG_DIR}/quality-configure.err.log"

cmake --build --preset "${CMAKE_BUILD_PRESET}" --parallel "${BUILD_JOBS}" \
  > "${LOG_DIR}/quality-build.out.log" 2> "${LOG_DIR}/quality-build.err.log"

ctest --preset "${TEST_PRESET}" --output-on-failure --output-junit "${LOG_DIR}/ctest-junit.xml" \
  > "${LOG_DIR}/quality-test.out.log" 2> "${LOG_DIR}/quality-test.err.log"

valgrind --leak-check=full --show-leak-kinds=all --error-exitcode=99 \
  --log-file="${LOG_DIR}/valgrind.log" \
  "${TEST_BIN}"

GCOVR_COMMON_ARGS=(--config /ws/ci/gcovr.cfg --root /ws --filter "^src/lib/" "${BUILD_DIR}")
gcovr "${GCOVR_COMMON_ARGS[@]}" --print-summary > "${LOG_DIR}/gcovr-summary.txt"
gcovr "${GCOVR_COMMON_ARGS[@]}" --xml-pretty --output "${LOG_DIR}/coverage-cobertura.xml"
gcovr "${GCOVR_COMMON_ARGS[@]}" --html-details --output "${LOG_DIR}/coverage.html"

perf stat \
  -e task-clock,context-switches,cpu-migrations,page-faults \
  --output "${LOG_DIR}/perf-stat.txt" \
  "${TEST_BIN}"
perf record \
  -F 99 \
  -g \
  --call-graph fp \
  --output "${LOG_DIR}/perf.data" \
  -- \
  "${TEST_BIN}" \
  --gtest_repeat="${PERF_REPEAT}" \
  --gtest_break_on_failure \
  --gtest_brief=1
perf report \
  --input "${LOG_DIR}/perf.data" \
  --stdio \
  > "${LOG_DIR}/perf-report.txt"
perf script --input "${LOG_DIR}/perf.data" > "${LOG_DIR}/perf.script"
"${FLAMEGRAPH_DIR}/stackcollapse-perf.pl" "${LOG_DIR}/perf.script" > "${LOG_DIR}/perf.folded"
grep -Ev "(testing::|std::|__gnu_cxx::|RUN_ALL_TESTS)" "${LOG_DIR}/perf.folded" > "${LOG_DIR}/perf.folded.filtered"
"${FLAMEGRAPH_DIR}/flamegraph.pl" \
  --title "Linux GCC fixed - calculator_tests" \
  "${LOG_DIR}/perf.folded.filtered" \
  > "${LOG_DIR}/flamegraph.svg"

python3 /ws/ci/scripts/render_quality_reports.py "${LOG_DIR}/coverage.html" "Coverage (gcovr)"
python3 /ws/ci/scripts/render_quality_reports.py "${LOG_DIR}/valgrind.log" "Valgrind"
python3 /ws/ci/scripts/render_quality_reports.py "${LOG_DIR}/perf-stat.txt" "Perf stat"
python3 /ws/ci/scripts/render_quality_reports.py "${LOG_DIR}/perf-report.txt" "Perf report"

chown -R "${HOST_UID}:${HOST_GID}" "${LOG_DIR}" "${BUILD_DIR}"

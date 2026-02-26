FROM rockylinux:9

# ---- Pinned versions ----
# On Rocky, the production way to "pin" GCC is by toolset major version
ARG GCC_TOOLSET=13

ARG CONAN_VERSION=2.0.14
ARG GCOVR_VERSION=8.5
ARG CMAKE_VERSION=3.29.8

# Base packages + CRB before installing ninja/toolsets
RUN dnf -y update \
    && dnf -y install dnf-plugins-core \
    && dnf config-manager --set-enabled crb \
    && dnf -y install epel-release \
    && dnf -y install \
        git curl-minimal unzip tar jq \
        python3 python3-pip \
        ninja-build make ccache \
        valgrind perf perl \
        openssl ca-certificates \
    && dnf clean all

# Install GCC toolset (major pinned)
RUN dnf -y install \
    "gcc-toolset-${GCC_TOOLSET}" \
    "gcc-toolset-${GCC_TOOLSET}-gcc-c++" \
    && dnf clean all

# Make toolset compilers default in PATH
ENV PATH="/opt/rh/gcc-toolset-${GCC_TOOLSET}/root/usr/bin:${PATH}"
ENV CC="gcc" CXX="g++"

# Install pinned CMake (binary installer)
RUN curl -fsSL -o /tmp/cmake.sh "https://github.com/Kitware/CMake/releases/download/v${CMAKE_VERSION}/cmake-${CMAKE_VERSION}-linux-x86_64.sh" \
 && chmod +x /tmp/cmake.sh \
 && /tmp/cmake.sh --skip-license --prefix=/usr/local \
 && rm -f /tmp/cmake.sh

# Install pinned Conan and gcovr
RUN pip3 install --no-cache-dir \
    "conan==${CONAN_VERSION}" \
    "gcovr==${GCOVR_VERSION}"

# Pre-install FlameGraph scripts used by perf analysis in CI
RUN git clone --depth 1 https://github.com/brendangregg/FlameGraph.git /opt/FlameGraph

# ---- Version proof (printed at image build time) ----
RUN set -eux; \
    echo "==== IMAGE: GCC ===="; \
    echo "Requested: gcc-toolset-${GCC_TOOLSET}"; \
    echo "--- gcc ---"; gcc --version | head -n 1; \
    echo "--- g++ ---"; g++ --version | head -n 1; \
    echo "--- ccache ---"; ccache --version | head -n 1; \
    echo "--- cmake ---"; cmake --version | head -n 1; \
    echo "--- ninja ---"; ninja --version; \
    echo "--- python ---"; python3 --version; \
    echo "--- conan ---"; conan --version; \
    echo "--- gcovr ---"; gcovr --version; \
    echo "--- valgrind ---"; valgrind --version | head -n 1; \
    echo "--- perf ---"; perf --version

CMD ["bash"]

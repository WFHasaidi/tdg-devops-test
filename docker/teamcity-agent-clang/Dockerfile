FROM rockylinux:9

# ---- Pinned versions ----
# Pin exact RPM version-release for the LLVM/Clang toolchain
ARG LLVM_RPM_VERSION=20.1.8-3.el9

ARG CONAN_VERSION=2.0.14
ARG CMAKE_VERSION=3.29.8

RUN dnf -y update \
    && dnf -y install dnf-plugins-core \
    && dnf config-manager --set-enabled crb \
    && dnf -y install \
        git curl-minimal unzip tar jq \
        python3 python3-pip \
        ninja-build make \
        openssl ca-certificates \
    && dnf clean all

# Install LLVM/Clang toolset (exact version pinned)
RUN dnf -y install \
    "llvm-toolset-${LLVM_RPM_VERSION}.x86_64" \
    "clang-${LLVM_RPM_VERSION}.x86_64" \
    "clang-tools-extra-${LLVM_RPM_VERSION}.x86_64" \
    && dnf clean all

ENV CC="clang" CXX="clang++"

RUN curl -fsSL -o /tmp/cmake.sh "https://github.com/Kitware/CMake/releases/download/v${CMAKE_VERSION}/cmake-${CMAKE_VERSION}-linux-x86_64.sh" \
 && chmod +x /tmp/cmake.sh \
 && /tmp/cmake.sh --skip-license --prefix=/usr/local \
 && rm -f /tmp/cmake.sh

RUN pip3 install --no-cache-dir "conan==${CONAN_VERSION}"

# ---- Version proof (printed at image build time) ----
RUN set -eux; \
    echo "==== IMAGE: CLANG ===="; \
    echo "Requested LLVM/Clang RPM version-release: ${LLVM_RPM_VERSION}"; \
    echo "--- clang ---"; clang --version | head -n 1; \
    echo "--- clang++ ---"; clang++ --version | head -n 1; \
    echo "--- cmake ---"; cmake --version | head -n 1; \
    echo "--- ninja ---"; ninja --version; \
    echo "--- python ---"; python3 --version; \
    echo "--- conan ---"; conan --version

CMD ["bash"]

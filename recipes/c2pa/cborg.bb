SUMMARY = "cborg - A simple C++ CBOR encoder/decoder"
DESCRIPTION = "Builds libcborg.a from TRUEPIC/cborg."
HOMEPAGE = "https://github.com/TRUEPIC/cborg"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://LICENSE;md5=635040556244c38145c5162ad045836d"

SRC_URI = "git://github.com/TRUEPIC/cborg.git;branch=master"
SRCREV = "2692a1b57e86ce6df6adb6a4499c578aa56a4bdc"

S = "${WORKDIR}/git"
B = "${WORKDIR}/build"

CXXFLAGS += "-std=c++11 -O2"

do_compile() {
    mkdir -p ${B}
    cd ${B}

    SRC_CPP_FILES="$(find ${S}/source -type f -name '*.cpp')"

    for cpp_file in ${SRC_CPP_FILES}; do
        obj="$(basename "${cpp_file}" .cpp).o"
        ${CXX} ${CXXFLAGS} -fPIC -I${S} -c "${cpp_file}" -o "${obj}"
    done

    ar rcs libcborg.a *.o
}

do_install() {
    # Install headers
    install -d ${D}${includedir}/cborg
    install -m 0644 ${S}/cborg/*.h ${D}${includedir}/cborg/

    # Install library
    install -d ${D}${libdir}
    install -m 0755 ${B}/libcborg.a ${D}${libdir}/
}

FILES:${PN}-dev += "${includedir}/cborg/*.h ${libdir}/libcborg.a"
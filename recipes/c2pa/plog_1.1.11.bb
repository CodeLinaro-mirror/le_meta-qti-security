DESCRIPTION = "Plog is a C++ logging library that is designed to be as simple, small and flexible as possible for logging purpose."
HOMEPAGE = "https://github.com/SergiusTheBest/plog"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=5e2a1e7f8253c4d42c1d8cd06eafdeed"

PV = "1.1.11"
SRC_URI = "git://github.com/SergiusTheBest/plog;branch=master"
SRCREV = "9c7ce5494d585031fab2b1685ed36ba0ffdbf45a"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${includedir}
    cp -r ${S}/include/* ${D}${includedir}/
}

FILES_${PN}-dev += "/plog"
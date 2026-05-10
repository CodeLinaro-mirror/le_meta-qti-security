DESCRIPTION = "Nlohmann JSON is a header only C++ library that offers intuitive, python-like syntax for JSON handling and trivial integration."
HOMEPAGE = "https://github.com/nlohmann/json"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE.MIT;md5=3b489645de9825cca5beeb9a7e18b6eb"

PV = "3.12.0"
SRC_URI = "git://github.com/nlohmann/json.git;branch=master"
SRCREV = "55f93686c01528224f448c19128836e7df245f72"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${includedir}
    cp -r ${S}/include/* ${D}${includedir}/
}

FILES:${PN}-dev += "/nlohmann"
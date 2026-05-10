DESCRIPTION = "stduuid is a lightweight, cross-platform UUID library for C++17"
HOMEPAGE = "https://github.com/mariusbancila/stduuid"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://LICENSE;md5=17a1d3575545a1e1c7c7f835388beafe"

PV = "1.2.3"
SRC_URI = "git://github.com/mariusbancila/stduuid;branch=master"
SRCREV = "3afe7193facd5d674de709fccc44d5055e144d7a"

S = "${WORKDIR}/git"

do_install() {
    install -d ${D}${includedir}/gsl
    install -m 0644 ${S}/include/uuid.h ${D}${includedir}
    cp -r ${S}/gsl/* ${D}${includedir}/gsl
}

FILES_${PN}-dev += "uuid.h /gsl"
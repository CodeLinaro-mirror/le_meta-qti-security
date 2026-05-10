DESCRIPTION = "TinyXML-2 is a simple, small, efficient, C++ XML parser."
HOMEPAGE = "https://github.com/leethomason/tinyxml2"
LICENSE = "Zlib"
LIC_FILES_CHKSUM = "file://LICENSE.txt;md5=135624eef03e1f1101b9ba9ac9b5fffd"

PV = "10.0.0"
SRC_URI = "git://github.com/leethomason/tinyxml2.git;branch=master"
SRCREV = "321ea883b7190d4e85cae5512a12e5eaa8f8731f"

S = "${WORKDIR}/git"

EXTRA_OECMAKE = "-DBUILD_SHARED_LIBS=ON"

do_install() {
    install -d ${D}${includedir}
    install -m 0644 ${S}/tinyxml2.h ${D}${includedir}
    install -d ${D}${libdir}
    install -m 0755 ${B}/libtinyxml2* ${D}${libdir}

    rm -f ${D}${libdir}/libtinyxml2.so
    ln -s libtinyxml2.so.${PV} ${D}${libdir}/libtinyxml2.so
}

FILES_${PN} += "${libdir}/libtinyxml2.so.*"
FILES_${PN}-dev += "${includedir}/tinyxml2.h ${libdir}/libtinyxml2.so"
SUMMARY = "QTI securemsm opensource packagegroup"

LICENSE = "BSD-3-Clause"
PACKAGE_ARCH = "${MACHINE_ARCH}"

inherit packagegroup

PACKAGES = ' \
    packagegroup-qti-securemsm \
'
RDEPENDS:${PN} += " \
    ${@bb.utils.contains("MACHINE_FEATURES", "qti-fscrypt", "fscrypt", "", d)} \
    "

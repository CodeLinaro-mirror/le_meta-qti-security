LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=801f80980d171dd6425610833a22dbe6"


DESCRIPTION = "Securemsm dlkm headers"
PR = "r1"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI = " \
    file://vendor/qcom/opensource/securemsm-kernel/ \
"
S = "${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/"

do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    LNX_INC_DIR=${D}/usr/include/linux/

    # smmu-proxy/include/uapi/linux
    install -d ${LNX_INC_DIR}/smmu-proxy/include/uapi/linux
    cp -r ${S}/smmu-proxy/include/uapi/linux/*.h ${LNX_INC_DIR}/smmu-proxy/include/uapi/linux/

    # smmu-proxy/linux
    install -d ${LNX_INC_DIR}/smmu-proxy/linux
    cp -r ${S}/smmu-proxy/linux/*.h ${LNX_INC_DIR}/smmu-proxy/linux/

    # include/uapi/linux/*
    install -d ${LNX_INC_DIR}/include/uapi/linux
    cp -r ${S}/include/uapi/linux/*.h ${LNX_INC_DIR}/include/uapi/linux/
}

PACKAGES = "${PN}"
FILES:${PN} += "/usr/*"

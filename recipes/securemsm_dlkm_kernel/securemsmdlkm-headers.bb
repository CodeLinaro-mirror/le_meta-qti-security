LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=801f80980d171dd6425610833a22dbe6"

DESCRIPTION = "Securemsm dlkm headers"
PR = "r1"

FILESPATH =+ "${WORKSPACE}:"
SRC_URI = " \
    file://vendor/qcom/opensource/securemsm-kernel/ \
"

S = "${WORKDIR}/vendor/qcom/opensource/securemsm-kernel"
do_configure[noexec] = "1"
do_compile[noexec] = "1"

do_install() {
    LNX_INC_DIR=${D}/usr/include/linux/

    # include/linux
    install -d ${LNX_INC_DIR}
    cp -r ${S}/include/linux/*.h ${LNX_INC_DIR}
    cp -r ${S}/include/uapi/linux/qseecom.h ${LNX_INC_DIR}
    cp -r ${S}/include/uapi/linux/qseecom_api.h ${LNX_INC_DIR}
    cp -r ${S}/smmu-proxy/include/uapi/linux/qti-smmu-proxy.h ${LNX_INC_DIR}

    # include/linux/misc
    LNX_INC_MISC_DIR=${D}/usr/include/linux/misc
    install -d ${LNX_INC_MISC_DIR}
    cp -r ${S}/linux/misc/*.h ${LNX_INC_MISC_DIR}

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

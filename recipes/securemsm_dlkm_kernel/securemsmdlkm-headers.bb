LICENSE = "GPL-2.0-only WITH Linux-syscall-note"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta-qti-security/recipes/securemsm_dlkm_kernel/\
licenses/COPYING;md5=6bc538ed5bd9a7fc9398086aedcd7e46"

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
    install -d ${D}/usr/include/linux/
    #cd ${KERNEL_OUT_PATH}/msm-kernel/

    for i in $(find ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/include/uapi/ -name "*.h" -printf "%P\n"); do
        cp -r ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/include/uapi/${i} ${D}/usr/include/${i}
    done

    for i in $(find ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/include/linux/ -name "*.h" -printf "%P\n"); do
        cp -r ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/include/linux/${i} ${D}/usr/include/linux/${i}
    done
}

PACKAGES = "${PN}"
FILES:${PN} += "/usr/*"

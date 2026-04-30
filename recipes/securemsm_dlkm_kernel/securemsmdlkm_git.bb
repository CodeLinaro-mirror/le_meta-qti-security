DESCRIPTION = "QTI securemsm drivers"
LICENSE = "GPL-2.0-with-autoconf-exception"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=966c02a95037a9c7ad75a7597aea9c5f"

inherit linux-kernel-base

PR = "r0"

DEPENDS += "rsync-native bc-native bison-native"
KERNEL_VERSION = "${@get_kernelversion_file("${STAGING_KERNEL_BUILDDIR}")}"

do_configure[depends] += "virtual/kernel:do_shared_workdir"

FILESPATH   =+ "${WORKSPACE}:"
SRC_URI = "file://vendor/qcom/opensource/securemsm-kernel/"
SRC_URI    +=  "file://start_smcinvoke_le"
SRC_URI    +=  "file://smcinvoke.service"
SRC_URI    +=  "file://qcedev.service"
SRC_URI    +=  "file://qrng.service"
SRC_URI    +=  "file://tz_log.service"
SRC_URI    +=  "file://smmu_proxy.service"
SRC_URI    +=  "file://qseecom.service"

S = "${WORKDIR}/vendor/qcom/opensource/securemsm-kernel"

EXTRA_OEMAKE += "TARGET_SUPPORT=${BASEMACHINE}"
EXT_KP_MODULES = "${@os.path.relpath("${S}","${KERNEL_PLATFORM_PATH}")}"

# Disable parallel make
PARALLEL_MAKE = "-j1"

STRIP_VERSION_MACHINE_FEATURES = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', '11.5.0', '9.3.0', d)}"
SIGN_PATH = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', 'dist', '../msm-kernel/scripts', d)}"
CERT_PATH = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', 'dist', '../msm-kernel/certs', d)}"
GCCVER_AVAILABLE := "${@''.join(filter(lambda x: x != '%', '${GCCVERSION}'))}.0"
STRIP_VERSION = "${@bb.utils.contains_any('BASEMACHINE', 'sa510m sdmsteppe alor vienna', '13.3.0', '${STRIP_VERSION_MACHINE_FEATURES}', d)}"
LD_PATH = "${@oe.utils.conditional('KERNEL_TOOLS_USES_MUSLC', 'True', "${LD_PATH_MUSLC}", "${LD_PATH_GLIBC}", d)}"

do_compile[lockfiles] = "${TMPDIR}/build_modules.lock"
do_compile[network] = "1"

do_configure() {
    cp -f ${WORKSPACE}/vendor/qcom/opensource/securemsm-kernel/Makefile ${WORKSPACE}/vendor/qcom/opensource/securemsm-kernel/Makefile.am
}

do_compile() {

    cd ${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform  && \

    BUILD_CONFIG=${KERNEL_BUILD_CONFIG} \
    EXT_MODULES=../../vendor/qcom/opensource/securemsm-kernel \
    ROOTDIR=${WORKSPACE}/ \
    ENABLE_DDK_BUILD=${DDK_BUILD} \
    TARGET_BOARD_PLATFORM=${TARGET_BOARD_PLATFORM} \
    VARIANT=${KERNEL_VARIANT} \
    MODULE_OUT=${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out \
    KERNEL_KIT=${KERNEL_OUT_PATH}/ \
    OUT_DIR=${KERNEL_OUT_PATH}/ \
    ./build/build_module.sh
}

do_strip_and_sign_modules() {

    install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko -D ${WORKDIR}/smcinvoke.ko

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qseecom_dlkm.ko -D ${WORKDIR}/qseecom.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko -D ${WORKDIR}/tz_log.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tmecom', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tmecom-intf_dlkm.ko -D ${WORKDIR}/tmecom.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko -D ${WORKDIR}/qce50.ko
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko -D ${WORKDIR}/qcedev-mod.ko
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko -D ${WORKDIR}/msm-rng.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smmu_proxy_dlkm.ko -D ${WORKDIR}/smmu_proxy.ko
    fi

    # strip debug symbols and sign the module
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', 'true', 'false', d)}; then
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qseecom_dlkm.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tmecom', 'true', 'false', d)}; then
        ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
            --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tmecom-intf_dlkm.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smmu_proxy_dlkm.ko
    fi


    # Since 5.10+ kernel with Techpack enabled SPs, module signing is no longer mandated, skipping.
    if ${@bb.utils.contains_any('BASEMACHINE', 'qrb5165 kalama qcs40x pineapple sdmsteppe alor vienna', 'false', 'true', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
        ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', 'true', 'false', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qseecom_dlkm.ko
        fi

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko
        fi

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tmecom', 'true', 'false', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tmecom-intf_dlkm.ko
        fi

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko
        fi

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smmu_proxy_dlkm.ko
        fi
    fi

}

python () {
    bb.build.addtask('do_strip_and_sign_modules', 'do_install', 'do_compile', d)
}

do_install() {
    install -d ${D}${systemd_unitdir}/system/multi-user.target.wants/
    install -d ${D}/usr/include/
    install -d ${D}/usr/lib/modules/

    cp -rp ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko ${D}${libdir}/modules/smcinvoke.ko
    chown 0:0 ${D}${libdir}/modules/smcinvoke.ko
    install -m 0644 ${WORKDIR}/smcinvoke.service -D ${D}${systemd_unitdir}/system/smcinvoke.service

    # /etc folder execute file/permission is disallow hence start_smcinvoke_le is move to /usr/sbin
    if ${@bb.utils.contains_any('BASEMACHINE', 'vienna alor', 'true', 'false', d)}; then
        install -d ${D}${sbindir}/initscripts
        install -m 0755 ${WORKDIR}/start_smcinvoke_le ${D}${sbindir}/initscripts
        sed -i 's|^ExecStart=/etc|ExecStart=/usr/sbin|' ${D}${systemd_unitdir}/system/smcinvoke.service
        sed -i 's|^ExecStop=/etc|ExecStop=/usr/sbin|' ${D}${systemd_unitdir}/system/smcinvoke.service
        sed -i 's|^SourcePath=/etc|SourcePath=/usr/sbin|' ${D}${systemd_unitdir}/system/smcinvoke.service
    else
        install -d ${D}${sysconfdir}/initscripts
        install -m 0755 ${WORKDIR}/start_smcinvoke_le ${D}${sysconfdir}/initscripts
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qseecom_dlkm.ko -D ${D}${libdir}/modules/${KERNEL_VERSION}/qseecom.ko
        install -d ${D}${sysconfdir}/modules-load.d/
        echo "qseecom" >> 01-qseecom.conf
        install -m 0644 01-qseecom.conf ${D}${sysconfdir}/modules-load.d/01-qseecom.conf
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko -D ${D}${libdir}/modules/tz_log.ko
        install -m 0644 ${WORKDIR}/tz_log.service -D ${D}${systemd_unitdir}/system/tz_log.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tmecom', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tmecom-intf_dlkm.ko -D ${D}${libdir}/modules/tmecom.ko
        sed -i '/^ExecStart=/i ExecStartPre=/sbin/insmod /usr/lib/modules/tmecom.ko' ${D}${systemd_unitdir}/system/tz_log.service
        sed -i 's|^ExecStop=/sbin/rmmod tz_log_dlkm|ExecStop=/sbin/rmmod tz_log_dlkm tmecom-intf_dlkm|' ${D}${systemd_unitdir}/system/tz_log.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko -D ${D}${libdir}/modules/qce50.ko
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko -D ${D}${libdir}/modules/qcedev-mod.ko
        install -m 0644 ${WORKDIR}/qcedev.service -D ${D}${systemd_unitdir}/system/qcedev.service
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko -D ${D}${libdir}/modules/msm-rng.ko
        install -m 0644 ${WORKDIR}/qrng.service -D ${D}${systemd_unitdir}/system/qrng.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        cp -rp ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smmu_proxy_dlkm.ko ${D}${libdir}/modules/smmu_proxy.ko
        chown 0:0 ${D}${libdir}/modules/smmu_proxy.ko
        install -m 0644 ${WORKDIR}/smmu_proxy.service -D ${D}${systemd_unitdir}/system/smmu_proxy.service
    fi

    cp -r ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/linux/ ${D}/usr/include/linux/
    cp -r ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/include/uapi/linux/qseecom.h ${D}/usr/include/linux/
    cp -r ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/include/uapi/linux/qseecom_api.h ${D}/usr/include/linux/
    cp -r ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/include/linux/ ${D}/usr/include/
    cp -r ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/smmu-proxy/include/uapi/linux ${D}/usr/include/
    ln -sf ${systemd_unitdir}/system/smcinvoke.service ${D}${systemd_unitdir}/system/multi-user.target.wants/smcinvoke.service

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
        ln -sf ${systemd_unitdir}/system/tz_log.service ${D}${systemd_unitdir}/system/multi-user.target.wants/tz_log.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
        ln -sf ${systemd_unitdir}/system/qcedev.service ${D}${systemd_unitdir}/system/multi-user.target.wants/qcedev.service
        ln -sf ${systemd_unitdir}/system/qrng.service ${D}${systemd_unitdir}/system/multi-user.target.wants/qrng.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        ln -sf ${systemd_unitdir}/system/smmu_proxy.service ${D}${systemd_unitdir}/system/multi-user.target.wants/smmu_proxy.service
    fi
}

FILES:${PN} += "${sysconfdir}/*"
FILES:${PN} += "${sbindir}/*"
FILES:${PN} += "${systemd_unitdir}/system/smcinvoke.service"
FILES:${PN} += "${systemd_unitdir}/system/multi-user.target.wants/smcinvoke.service"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', "${systemd_unitdir}/system/qcedev.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', "${systemd_unitdir}/system/multi-user.target.wants/qcedev.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', "${systemd_unitdir}/system/qrng.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', "${systemd_unitdir}/system/multi-user.target.wants/qrng.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', "${systemd_unitdir}/system/smmu_proxy.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', "${systemd_unitdir}/system/multi-user.target.wants/smmu_proxy.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', "${systemd_unitdir}/system/tz_log.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', "${systemd_unitdir}/system/multi-user.target.wants/tz_log.service", "", d)}"
FILES:${PN} += "${libdir}/modules/*"

RM_WORK_EXCLUDE += "${PN}"

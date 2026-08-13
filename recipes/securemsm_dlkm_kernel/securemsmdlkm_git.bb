DESCRIPTION = "QTI securemsm drivers"
LICENSE = "GPL-2.0-with-autoconf-exception"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=966c02a95037a9c7ad75a7597aea9c5f"

inherit linux-kernel-base

PR = "r0"

DEPENDS = "rsync-native"
DEPENDS += "bc-native bison-native"

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

SIGN_PATH = "${@bb.utils.contains('BASEMACHINE', 'sa510m', 'dist', \
               bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', 'dist', '../msm-kernel/certs', d), d)}"

CERT_PATH = "${@bb.utils.contains('BASEMACHINE', 'sa510m', 'dist', \
               bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', 'dist', '../msm-kernel/certs', d), d)}"

STRIP_VERSION = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', '${KP_STRIP_VERSION}', '9.3.0', d)}"
GCCVER_AVAILABLE = "${@''.join(filter(lambda x: x != '%', '${GCCVERSION}'))}.0"
STRIP_VERSION = "${@bb.utils.contains_any('BASEMACHINE', 'sa510m', '13.3.0', '${GCCVER_AVAILABLE}', d)}"

LD_PATH = "${@oe.utils.conditional('KERNEL_TOOLS_USES_MUSLC', 'True', "${LD_PATH_MUSLC}", "${LD_PATH_GLIBC}", d)}"


do_compile[lockfiles] = "${TMPDIR}/build_modules.lock"

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
    VARIANT=${KERNEL_DEFCONFIG_VARIANT} \
    MODULE_OUT=${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out \
    KERNEL_KIT=${KERNEL_OUT_PATH}/ \
    OUT_DIR=temp_out_dir \
    ./build/build_module.sh
}

do_compile:sa510m() {
    variant="${@bb.utils.contains('DEBUG_BUILD','1', "debug", "perf", d)}"
    cd ${KERNEL_PLATFORM_PATH}
    ENABLE_DDK_BUILD="true" \
    BUILD_CONFIG=${KERNEL_BUILD_CONFIG} \
    EXT_MODULES=${EXT_KP_MODULES} \
    ROOTDIR=${WORKSPACE}/ \
    KERNEL_KIT=${KERNEL_PREBUILT_PATH} \
    VARIANT=${variant}_defconfig \
    OUT_DIR=${KERNEL_OUT_PATH} \
    MODULE_OUT=${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out \
    TARGET_BOARD_PLATFORM=${TARGET_BOARD_PLATFORM} \
    ./build/build_module.sh
}

do_compile:sa510m_1g() {
    variant="${@bb.utils.contains('DEBUG_BUILD','1', "debug", "perf", d)}"
    cd ${KERNEL_PLATFORM_PATH}
    ENABLE_DDK_BUILD="true" \
    BUILD_CONFIG=${KERNEL_BUILD_CONFIG} \
    EXT_MODULES=${EXT_KP_MODULES} \
    ROOTDIR=${WORKSPACE}/ \
    KERNEL_KIT=${KERNEL_PREBUILT_PATH} \
    VARIANT=${variant}_defconfig \
    OUT_DIR=${KERNEL_OUT_PATH} \
    MODULE_OUT=${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out \
    TARGET_BOARD_PLATFORM=${TARGET_BOARD_PLATFORM} \
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

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)} ||
       ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko -D ${WORKDIR}/qce50.ko
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko -D ${WORKDIR}/qcedev-mod.ko
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko -D ${WORKDIR}/msm-rng.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smmu_proxy_dlkm.ko -D ${WORKDIR}/smmu_proxy.ko
    fi

    # strip debug symbols
    ${STAGING_DIR_NATIVE}/usr/libexec/${TARGET_SYS}/gcc/${TARGET_SYS}/${STRIP_VERSION}/strip \
        --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', 'true', 'false', d)}; then
        ${STAGING_DIR_NATIVE}/usr/libexec/${TARGET_SYS}/gcc/${TARGET_SYS}/${STRIP_VERSION}/strip \
            --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qseecom_dlkm.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
        ${STAGING_DIR_NATIVE}/usr/libexec/${TARGET_SYS}/gcc/${TARGET_SYS}/${STRIP_VERSION}/strip \
            --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)} ||
       ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
        ${STAGING_DIR_NATIVE}/usr/libexec/${TARGET_SYS}/gcc/${TARGET_SYS}/${STRIP_VERSION}/strip \
            --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko
        ${STAGING_DIR_NATIVE}/usr/libexec/${TARGET_SYS}/gcc/${TARGET_SYS}/${STRIP_VERSION}/strip \
            --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko
        ${STAGING_DIR_NATIVE}/usr/libexec/${TARGET_SYS}/gcc/${TARGET_SYS}/${STRIP_VERSION}/strip \
            --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        ${STAGING_DIR_NATIVE}/usr/libexec/${TARGET_SYS}/gcc/${TARGET_SYS}/${STRIP_VERSION}/strip \
            --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smmu_proxy_dlkm.ko
    fi

    TOOLS_LIB="${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/prebuilts/kernel-build-tools/linux-x86/lib64"
    DIST_LIB="${KERNEL_PREBUILT_PATH}/${SIGN_PATH}"
    export LD_LIBRARY_PATH="${TOOLS_LIB}:${DIST_LIB}:${LD_LIBRARY_PATH}"

    # Disable module signing for securemsm DLKM techpack module
    if ${@bb.utils.contains_any('BASEMACHINE', 'sun kera', 'false', 'true', d)}; then
        ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
        ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', 'true', 'false', d)}; then
            ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qseecom_dlkm.ko
        fi

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
            ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko
        fi

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)} ||
           ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
                ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
                ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko
                ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
                ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko
                ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
                ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko
	fi

	if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)}; then
            ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko
        fi

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
            ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smmu_proxy_dlkm.ko
        fi
    fi

         if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)}; then
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

}

python () {
    bb.build.addtask('do_strip_and_sign_modules', 'do_install', 'do_compile', d)
}

do_install() {
    install -d ${D}${systemd_unitdir}/system/multi-user.target.wants/
    install -d ${D}/usr/include/

    cp -rp ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko ${D}${libdir}/modules/smcinvoke.ko
    chown 0:0 ${D}${libdir}/modules/smcinvoke.ko
    install -m 0644 ${WORKDIR}/smcinvoke.service -D ${D}${systemd_unitdir}/system/smcinvoke.service

    # /etc folder execute file/permission is disallow hence start_smcinvoke_le is move to /usr/sbin
    if ${@bb.utils.contains('BASEMACHINE', 'kera', 'true', 'false', d)}; then
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
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qseecom_dlkm.ko -D ${D}${libdir}/modules/qseecom.ko
        install -m 0644 ${WORKDIR}/qseecom.service -D ${D}${systemd_unitdir}/system/qseecom.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko -D ${D}${libdir}/modules/tz_log.ko
        install -m 0644 ${WORKDIR}/tz_log.service -D ${D}${systemd_unitdir}/system/tz_log.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)} ||
       ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
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

    ln -sf ${systemd_unitdir}/system/smcinvoke.service ${D}${systemd_unitdir}/system/multi-user.target.wants/smcinvoke.service

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', 'true', 'false', d)}; then
        ln -sf ${systemd_unitdir}/system/qseecom.service ${D}${systemd_unitdir}/system/multi-user.target.wants/qseecom.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
        ln -sf ${systemd_unitdir}/system/tz_log.service ${D}${systemd_unitdir}/system/multi-user.target.wants/tz_log.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)} ||
       ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
        ln -sf ${systemd_unitdir}/system/qcedev.service ${D}${systemd_unitdir}/system/multi-user.target.wants/qcedev.service
        ln -sf ${systemd_unitdir}/system/qrng.service ${D}${systemd_unitdir}/system/multi-user.target.wants/qrng.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        ln -sf ${systemd_unitdir}/system/smmu_proxy.service ${D}${systemd_unitdir}/system/multi-user.target.wants/smmu_proxy.service
    fi
}

FILES:${PN} += "${sysconfdir}/*"
FILES:${PN} += "/etc/initscripts/start_smcinvoke_le"
FILES:${PN} += "${systemd_unitdir}/system/smcinvoke.service"
FILES:${PN} += "${systemd_unitdir}/system/multi-user.target.wants/smcinvoke.service"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', "${systemd_unitdir}/system/qseecom.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', "${systemd_unitdir}/system/multi-user.target.wants/qseecom.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', "${systemd_unitdir}/system/qcedev.service", "", d) or \
bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', "${systemd_unitdir}/system/qcedev.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', "${systemd_unitdir}/system/multi-user.target.wants/qcedev.service", "", d) or \
bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', "${systemd_unitdir}/system/multi-user.target.wants/qcedev.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', "${systemd_unitdir}/system/qrng.service", "", d) or \
bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', "${systemd_unitdir}/system/qrng.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', "${systemd_unitdir}/system/multi-user.target.wants/qrng.service", "", d) or \
bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', "${systemd_unitdir}/system/multi-user.target.wants/qrng.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', "${systemd_unitdir}/system/smmu_proxy.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', "${systemd_unitdir}/system/multi-user.target.wants/smmu_proxy.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', "${systemd_unitdir}/system/tz_log.service", "", d)}"
FILES:${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', "${systemd_unitdir}/system/multi-user.target.wants/tz_log.service", "", d)}"
FILES:${PN} += "${libdir}/modules/*"

RM_WORK_EXCLUDE += "${PN}"

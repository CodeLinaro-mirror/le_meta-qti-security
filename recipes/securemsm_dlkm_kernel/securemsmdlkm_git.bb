DESCRIPTION = "QTI securemsm drivers"
LICENSE = "GPL-2.0"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=801f80980d171dd6425610833a22dbe6"

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

S = "${WORKDIR}/vendor/qcom/opensource/securemsm-kernel"

EXTRA_OEMAKE += "TARGET_SUPPORT=${BASEMACHINE}"

# Disable parallel make
PARALLEL_MAKE = "-j1"

do_configure() {
    cp -f ${WORKSPACE}/vendor/qcom/opensource/securemsm-kernel/Makefile ${WORKSPACE}/vendor/qcom/opensource/securemsm-kernel/Makefile.am
}

do_compile() {

    cd ${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform  && \

    BUILD_CONFIG=${KERNEL_BUILD_CONFIG} \
    EXT_MODULES=../../vendor/qcom/opensource/securemsm-kernel \
    ROOTDIR=${WORKSPACE}/ \
    MODULE_OUT=${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out \
    KERNEL_KIT=${KERNEL_OUT_PATH}/ \
    OUT_DIR=temp_out_dir \
    ./build/build_module.sh
}

do_install() {
    install -d ${D}${sysconfdir}/initscripts
    install -d ${D}${systemd_unitdir}/system/multi-user.target.wants/
    install -d ${D}/usr/include/
    install -d ${D}/usr/lib/modules/
    install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko -D ${WORKDIR}/smcinvoke.ko
    install -m 0755 ${WORKDIR}/start_smcinvoke_le ${D}${sysconfdir}/initscripts

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko -D ${WORKDIR}/qce50.ko
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko -D ${WORKDIR}/qcedev-mod.ko
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko -D ${WORKDIR}/msm-rng.ko
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko -D ${WORKDIR}/tz_log.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smmu_proxy_dlkm.ko -D ${WORKDIR}/smmu_proxy.ko
    fi

        # strip debug symbols and sign the module
        ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/9.3.0/strip \
              --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko

              if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)}; then
                ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/9.3.0/strip \
                    --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko
                ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/9.3.0/strip \
                    --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko
                ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/9.3.0/strip \
                    --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko
                ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/9.3.0/strip \
                    --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko
              fi

              if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
                ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/9.3.0/strip \
                    --strip-debug ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smmu_proxy_dlkm.ko
              fi

        LD_LIBRARY_PATH=${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/prebuilts/kernel-build-tools/linux-x86/lib64/ \
        ${KERNEL_PREBUILT_PATH}/../msm-kernel/scripts/sign-file sha1 ${KERNEL_PREBUILT_PATH}/../msm-kernel/certs/signing_key.pem \
             ${KERNEL_PREBUILT_PATH}/../msm-kernel/certs/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko

             if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)}; then
                LD_LIBRARY_PATH=${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/prebuilts/kernel-build-tools/linux-x86/lib64/ \
                ${KERNEL_PREBUILT_PATH}/../msm-kernel/scripts/sign-file sha1 ${KERNEL_PREBUILT_PATH}/../msm-kernel/certs/signing_key.pem \
                    ${KERNEL_PREBUILT_PATH}/../msm-kernel/certs/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko
		LD_LIBRARY_PATH=${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/prebuilts/kernel-build-tools/linux-x86/lib64/ \
                ${KERNEL_PREBUILT_PATH}/../msm-kernel/scripts/sign-file sha1 ${KERNEL_PREBUILT_PATH}/../msm-kernel/certs/signing_key.pem \
                    ${KERNEL_PREBUILT_PATH}/../msm-kernel/certs/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko
                LD_LIBRARY_PATH=${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/prebuilts/kernel-build-tools/linux-x86/lib64/ \
                ${KERNEL_PREBUILT_PATH}/../msm-kernel/scripts/sign-file sha1 ${KERNEL_PREBUILT_PATH}/../msm-kernel/certs/signing_key.pem \
                    ${KERNEL_PREBUILT_PATH}/../msm-kernel/certs/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko
                LD_LIBRARY_PATH=${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform/prebuilts/kernel-build-tools/linux-x86/lib64/ \
                ${KERNEL_PREBUILT_PATH}/../msm-kernel/scripts/sign-file sha1 ${KERNEL_PREBUILT_PATH}/../msm-kernel/certs/signing_key.pem \
                    ${KERNEL_PREBUILT_PATH}/../msm-kernel/certs/signing_key.x509 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko
             fi

    install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smcinvoke_dlkm.ko -D ${D}${libdir}/modules/smcinvoke.ko
    install -m 0644 ${WORKDIR}/smcinvoke.service -D ${D}${systemd_unitdir}/system/smcinvoke.service

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qce50_dlkm.ko -D ${D}${libdir}/modules/qce50.ko
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qcedev-mod_dlkm.ko -D ${D}${libdir}/modules/qcedev-mod.ko
        install -m 0644 ${WORKDIR}/qcedev.service -D ${D}${systemd_unitdir}/system/qcedev.service
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/qrng_dlkm.ko -D ${D}${libdir}/modules/msm-rng.ko
        install -m 0644 ${WORKDIR}/qrng.service -D ${D}${systemd_unitdir}/system/qrng.service
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/tz_log_dlkm.ko -D ${D}${libdir}/modules/tz_log.ko
        install -m 0644 ${WORKDIR}/tz_log.service -D ${D}${systemd_unitdir}/system/tz_log.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        install -m 0755 ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel-out/smmu_proxy_dlkm.ko -D ${D}${libdir}/modules/smmu_proxy.ko
        install -m 0644 ${WORKDIR}/smmu_proxy.service -D ${D}${systemd_unitdir}/system/smmu_proxy.service
    fi

    # TODO: copy UAPI headers
    cp -r ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/linux/ ${D}/usr/include/linux/
    cp -r ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/include/linux/ ${D}/usr/include/
    cp -r ${WORKDIR}/vendor/qcom/opensource/securemsm-kernel/smmu-proxy/uapi/linux ${D}/usr/include/
    ln -sf ${systemd_unitdir}/system/smcinvoke.service ${D}${systemd_unitdir}/system/multi-user.target.wants/smcinvoke.service

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', 'true', 'false', d)}; then
        ln -sf ${systemd_unitdir}/system/qcedev.service ${D}${systemd_unitdir}/system/multi-user.target.wants/qcedev.service
        ln -sf ${systemd_unitdir}/system/qrng.service ${D}${systemd_unitdir}/system/multi-user.target.wants/qrng.service
        ln -sf ${systemd_unitdir}/system/qrng.service ${D}${systemd_unitdir}/system/multi-user.target.wants/tz_log.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        ln -sf ${systemd_unitdir}/system/smmu_proxy.service ${D}${systemd_unitdir}/system/multi-user.target.wants/smmu_proxy.service
    fi
}

FILES_${PN} += "${sysconfdir}/*"
FILES_${PN} += "/etc/initscripts/start_smcinvoke_le"
FILES_${PN} += "${systemd_unitdir}/system/smcinvoke.service"
FILES_${PN} += "${systemd_unitdir}/system/multi-user.target.wants/smcinvoke.service"

FILES_${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', "${systemd_unitdir}/system/qcedev.service", "", d)}"
FILES_${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', "${systemd_unitdir}/system/multi-user.target.wants/qcedev.service", "", d)}"
FILES_${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', "${systemd_unitdir}/system/qrng.service", "", d)}"
FILES_${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', "${systemd_unitdir}/system/multi-user.target.wants/qrng.service", "", d)}"
FILES_${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', "${systemd_unitdir}/system/tz_log.service", "", d)}"
FILES_${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-csm', "${systemd_unitdir}/system/multi-user.target.wants/tz_log.service", "", d)}"
FILES_${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', "${systemd_unitdir}/system/smmu_proxy.service", "", d)}"
FILES_${PN} += "${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', "${systemd_unitdir}/system/multi-user.target.wants/smmu_proxy.service", "", d)}"

FILES_${PN} += "${libdir}/modules/*"

DESCRIPTION = "QTI securemsm drivers"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/\
${LICENSE};md5=801f80980d171dd6425610833a22dbe6"

inherit linux-kernel-base module

PR = "r0"

COMPATIBLE_MACHINE = "pebble"
DEFAULT_PREFERENCE = "-1"

DEPENDS += "bc-native bison-native virtual/kernel virtual/kernel-toolchain-native linux-msm-headers"

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
PROVIDES = "kernel-module-securemsmdlkm"

DLKM_RPROVIDES_MAP += "kernel-module-tmecom-${KERNEL_VERSION}:kernel-module-tmecom-intf-dlkm-${KERNEL_VERSION}"

GCCVER_AVAILABLE := "${@''.join(filter(lambda x: x != '%', '${GCCVERSION}'))}.0"
STRIP_VERSION_MACHINE_FEATURES = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', '${GCCVER_AVAILABLE}', '9.3.0', d)}"
SIGN_PATH = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', 'dist', '../msm-kernel/scripts', d)}"
CERT_PATH = "${@bb.utils.contains('MACHINE_FEATURES', 'qti-vm-target', 'dist', '../msm-kernel/certs', d)}"
STRIP_VERSION = "${GCCVER_AVAILABLE}"
LD_PATH = "${@oe.utils.conditional('KERNEL_TOOLS_USES_MUSLC', 'True', "${LD_PATH_MUSLC}", "${LD_PATH_GLIBC}", d)}"

do_compile[umask] = "000"

# ============ Build parameters ============
EXTRA_OEMAKE:append = " M=${S}"
EXTRA_OEMAKE:append = " TARGET_SUPPORT=${BASEMACHINE}"
EXTRA_OEMAKE:append = " TARGET_MACHINE=${MACHINE}"
EXTRA_OEMAKE:append = " KCPPFLAGS='-I${STAGING_INCDIR} -I${STAGING_INCDIR}/soc-repo -I${STAGING_INCDIR}/soc-repo/uapi'"

# ============ Toolchain configuration ============
KERNEL_CC = "${STAGING_BINDIR_NATIVE}/clang/bin/clang \
             -target ${TARGET_ARCH}${TARGET_VENDOR}-${TARGET_OS}"
KERNEL_LD = "${STAGING_BINDIR_NATIVE}/clang/bin/ld.lld"
KERNEL_AR = "${STAGING_BINDIR_NATIVE}/clang/bin/llvm-ar"
KERNEL_OBJCOPY = "${STAGING_BINDIR_NATIVE}/clang/bin/llvm-objcopy"
KERNEL_STRIP = "${STAGING_BINDIR_NATIVE}/clang/bin/llvm-strip"
MAKE_TARGETS = "modules"

SOCINCLUDE = "-I${STAGING_INCDIR}/soc-repo"
EXTRA_OEMAKE:append = " SOCINCLUDE='${SOCINCLUDE}'"

EXTRA_OEMAKE:append = " \
  KCPPFLAGS+=' -I${RECIPE_SYSROOT}${includedir}/linux-msm/usr/include' \
  KCFLAGS+=' -I${RECIPE_SYSROOT}${includedir}/linux-msm/usr/include' \
  KCFLAGS+=' -I${S}/include ' \
"

do_configure() {
    cp -f ${WORKSPACE}/vendor/qcom/opensource/securemsm-kernel/Makefile ${WORKSPACE}/vendor/qcom/opensource/securemsm-kernel/Makefile.am
}

do_strip_and_sign_modules() {

    install -m 0755 ${S}/smcinvoke_dlkm.ko -D ${WORKDIR}/smcinvoke.ko

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', 'true', 'false', d)}; then
        install -m 0755 ${S}/qseecom_dlkm.ko -D ${WORKDIR}/qseecom.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
        install -m 0755 ${S}/tz_log_dlkm.ko -D ${WORKDIR}/tz_log.ko
        install -m 0755 ${S}/tmecom-intf_dlkm.ko -D ${WORKDIR}/tmecom-intf.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
        install -m 0755 ${S}/qce50_dlkm.ko -D ${WORKDIR}/qce50.ko
        install -m 0755 ${S}/qcedev-mod_dlkm.ko -D ${WORKDIR}/qcedev-mod.ko
        install -m 0755 ${S}/qrng_dlkm.ko -D ${WORKDIR}/msm-rng.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        install -m 0755 ${S}/smmu_proxy_dlkm.ko -D ${WORKDIR}/smmu_proxy.ko
    fi

    # strip debug symbols and sign the module
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${S}/smcinvoke_dlkm.ko

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', 'true', 'false', d)}; then
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${S}/qseecom_dlkm.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${S}/tz_log_dlkm.ko
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${S}/tmecom-intf_dlkm.ko

    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${S}/qce50_dlkm.ko
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${S}/qcedev-mod_dlkm.ko
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${S}/qrng_dlkm.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
    ${STAGING_DIR_NATIVE}/usr/libexec/aarch64-oe-linux/gcc/aarch64-oe-linux/${STRIP_VERSION}/strip \
        --strip-debug ${S}/smmu_proxy_dlkm.ko
    fi

    # Since 5.10+ kernel with Techpack enabled SPs, module signing is no longer mandated, skipping.
    if ${@bb.utils.contains_any('BASEMACHINE', 'qrb5165 kalama qcs40x pineapple sdmsteppe pebble', 'false', 'true', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
        ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${S}/smcinvoke_dlkm.ko

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-qseecom', 'true', 'false', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${S}/qseecom_dlkm.ko
        fi

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${S}/tz_log_dlkm.ko

        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${S}/tmecom-intf_dlkm.ko

        fi

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${S}/qce50_dlkm.ko
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${S}/qcedev-mod_dlkm.ko
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${S}/qrng_dlkm.ko
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${S}/tz_log_dlkm.ko
        fi

        if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        LD_LIBRARY_PATH=${LD_PATH} ${KERNEL_PREBUILT_PATH}/${SIGN_PATH}/sign-file sha1 ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.pem \
            ${KERNEL_PREBUILT_PATH}/${CERT_PATH}/signing_key.x509 ${S}/smmu_proxy_dlkm.ko
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

    cp -rp ${S}/smcinvoke_dlkm.ko ${D}${libdir}/modules/smcinvoke.ko
    chown 0:0 ${D}${libdir}/modules/smcinvoke.ko
    install -m 0644 ${WORKDIR}/smcinvoke.service -D ${D}${systemd_unitdir}/system/smcinvoke.service

    # /etc folder execute file/permission is disallow hence start_smcinvoke_le is move to /usr/sbin
    if ${@bb.utils.contains_any('BASEMACHINE', 'vienna alor pebble', 'true', 'false', d)}; then
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
        install -m 0755 ${S}/qseecom_dlkm.ko -D ${D}${libdir}/modules/${KERNEL_VERSION}/qseecom.ko
        install -d ${D}${sysconfdir}/modules-load.d/
        echo "qseecom" >> 01-qseecom.conf
        install -m 0644 01-qseecom.conf ${D}${sysconfdir}/modules-load.d/01-qseecom.conf
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-tzlog', 'true', 'false', d)}; then
        install -m 0755 ${S}/tz_log_dlkm.ko -D ${D}${libdir}/modules/tz_log.ko
        install -m 0644 ${WORKDIR}/tz_log.service -D ${D}${systemd_unitdir}/system/tz_log.service
        install -m 0755 ${S}/tmecom-intf_dlkm.ko -D ${D}${libdir}/modules/tmecom-intf.ko
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-crypto', 'true', 'false', d)}; then
        install -m 0755 ${S}/qce50_dlkm.ko -D ${D}${libdir}/modules/qce50.ko
        install -m 0755 ${S}/qcedev-mod_dlkm.ko -D ${D}${libdir}/modules/qcedev-mod.ko
        install -m 0644 ${WORKDIR}/qcedev.service -D ${D}${systemd_unitdir}/system/qcedev.service
        install -m 0755 ${S}/qrng_dlkm.ko -D ${D}${libdir}/modules/msm-rng.ko
        install -m 0644 ${WORKDIR}/qrng.service -D ${D}${systemd_unitdir}/system/qrng.service
    fi

    if ${@bb.utils.contains('MACHINE_FEATURES', 'qti-smmu-proxy', 'true', 'false', d)}; then
        cp -rp ${S}/smmu_proxy_dlkm.ko ${D}${libdir}/modules/smmu_proxy.ko
        chown 0:0 ${D}${libdir}/modules/smmu_proxy.ko
        install -m 0644 ${WORKDIR}/smmu_proxy.service -D ${D}${systemd_unitdir}/system/smmu_proxy.service
    fi

    cp -r ${S}/linux/ ${D}/usr/include/linux/
    cp -r ${S}/include/uapi/linux/qseecom.h ${D}/usr/include/linux/
    cp -r ${S}/include/uapi/linux/qseecom_api.h ${D}/usr/include/linux/
    cp -r ${S}/include/linux/ ${D}/usr/include/
    cp -r ${S}/smmu-proxy/include/uapi/linux ${D}/usr/include/
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

    install -Dm0644 ${B}/Module.symvers ${D}${includedir}/kernel-module-${BPN}/Module.symvers
}

PACKAGESPLITFUNCS:append = " add_dlkm_rprovides"

python add_dlkm_rprovides () {
    kv = d.getVar('KERNEL_VERSION') or ''
    pkgs = (d.getVar('PACKAGES') or '').split()

    for p in pkgs:
        if not p.startswith('kernel-module-'):
            continue
        if kv and not p.endswith('-' + kv):
            continue
        if '-dlkm-' in p:
            continue

        alias = p[:-len(kv)-1] + '-dlkm-' + kv if kv else (p + '-dlkm')

        cur = d.getVar('RPROVIDES:' + p) or ''
        if alias not in cur.split():
            d.setVar('RPROVIDES:' + p, (cur + ' ' + alias).strip())

    m = (d.getVar('DLKM_RPROVIDES_MAP') or '').split()
    for item in m:
        if ':' not in item:
            continue
        frm, to = item.split(':', 1)
        if frm not in pkgs:
            continue
        cur = d.getVar('RPROVIDES:' + frm) or ''
        if to not in cur.split():
            d.setVar('RPROVIDES:' + frm, (cur + ' ' + to).strip())
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

DESCRIPTION = "Start up script for crypto modules installation"
LICENSE = "BSD-3-Clause"
LIC_FILES_CHKSUM = "file://${COREBASE}/meta/files/common-licenses/${LICENSE};md5=550794465ba0ec5312d6919e203a55f9"

SRC_URI +="file://qce_dlkm"
SRC_URI +="file://qce_dlkm.service"

PR = "r0"

INITSCRIPT_NAME = "qce_dlkm"
INITSCRIPT_PARAMS = "start 80 2 3 4 5 . stop 20 0 1 6 ."

FILES_${PN} += "${systemd_unitdir}/system/"

do_install_append() {
	if ${@bb.utils.contains('DISTRO_FEATURES', 'systemd', 'true', 'false', d)}; then
		install -m 0755 ${WORKDIR}/qce_dlkm -D ${D}${sysconfdir}/initscripts/${INITSCRIPT_NAME}
		install -d ${D}/etc/systemd/system/
		install -m 0644 ${WORKDIR}/qce_dlkm.service -D ${D}/etc/systemd/system/qce_dlkm.service
		install -d ${D}/etc/systemd/system/multi-user.target.wants/
		# enable the service for multi-user.target
		ln -sf /etc/systemd/system/qce_dlkm.service \
			${D}/etc/systemd/system/multi-user.target.wants/qce_dlkm.service
	else
		install -m 0755 ${WORKDIR}/qce_dlkm -D ${D}${sysconfdir}/init.d/${INITSCRIPT_NAME}
fi
}

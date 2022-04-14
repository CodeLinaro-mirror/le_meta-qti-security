DESCRIPTION = "The Go tool for managing Linux filesystem encryption"

SRC_URI = "git://git.codelinaro.org/clo/le/platform/external/google/fscrypt;branch=caf_migration/github-google/master;tag=v0.2.9;protocol=git"
SRC_URI += "file://0001-Add-changes-for-inline-encrypt.patch"
SRC_URI_append += " ${@bb.utils.contains('GCCVERSION', '9.3%', 'file://0001-solve-aarch64-oe-linux-gcc-build-error.patch', '', d)}"
SRC_URI_append += " ${@bb.utils.contains('GCCVERSION', '9.3%', 'file://fscrypt', '', d)}"

LICENSE = "Apache-2.0"
LIC_FILES_CHKSUM = "file://${WORKDIR}/${PN}-${PV}/src/${GO_IMPORT}/LICENSE;md5=3b83ef96387f14655fc854ddc3c6bd57"

inherit go
GO_IMPORT = "github.com/google/fscrypt"

DEPENDS = "libpam"

#required as of Go 1.12
GOCACHE = "${WORKDIR}/.cache/go-build"

GOROOT = "/usr/local/go"
CGO_LDFLAGS += "--sysroot ${STAGING_DIR_TARGET}"
CGO_CPPFLAGS += "--sysroot ${STAGING_DIR_TARGET}"

do_package_qa[noexec]="1"

do_patch() {
	cd ${WORKDIR}/${PN}-${PV}/src/${GO_IMPORT}
	patch -p1 < ${WORKDIR}/0001-Add-changes-for-inline-encrypt.patch
	if ${@bb.utils.contains('GCCVERSION', '9.3%', 'true', 'false', d)}; then
		patch -p1 < ${WORKDIR}/0001-solve-aarch64-oe-linux-gcc-build-error.patch
	fi
}

do_compile() {
	unset GOPATH
	cd ${WORKDIR}/${PN}-${PV}/src/${GO_IMPORT}
	GOOS=${TARGET_GOOS} GOARCH=${TARGET_GOARCH} CGO_ENABLED=1 CC="${CC}" ${MAKE}
	cd -
}

do_install() {
	install -d ${D}${bindir}
	install -d ${D}${libdir}
	if ${@bb.utils.contains('GCCVERSION', '9.3%', 'true', 'false', d)}; then
		install -d ${D}/etc/pam.d/
		install -m 0644 ${WORKDIR}/fscrypt -D  ${D}/etc/pam.d/fscrypt
	fi
	install -m 0755 ${WORKDIR}/${PN}-${PV}/src/${GO_IMPORT}/bin/fscrypt ${D}${bindir}/
	install -m 0755 ${WORKDIR}/${PN}-${PV}/src/${GO_IMPORT}/bin/pam_fscrypt.so ${D}${libdir}/
}

FILES_${PN} += "${bindir}/fscrypt \
                ${libdir}/pam_fscrypt.so"

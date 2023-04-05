include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=securemsm-dlkm-kernel
PKG_VERSION:=1.0
LOCAL_SRC := $(TOPDIR)/src/vendor/qcom/opensource/securemsm-kernel
include $(INCLUDE_DIR)/package.mk

define KernelPackage/$(PKG_NAME)
	SECTION:=Kernel Modules
	CATEGORY:=External Kernel Modules
	TITLE:=securemsm_dlkm_kernel
endef

define KernelPackage/$(PKG_NAME)/Description
	securemsmdlkm packages
endef

define Build/Prepare
	mkdir -p $(PKG_BUILD_DIR)/
	$(CP) $(LOCAL_SRC)/* $(PKG_BUILD_DIR)/
endef

MAKE_OPTS:= \
	M="$(PKG_BUILD_DIR)"

define Build/Compile
	$(MAKE) -C "$(LINUX_DIR)" \
	$(MAKE_OPTS) \
	modules
endef

define Build/InstallDev
	$(INSTALL_DIR) $(STAGING_DIR)
	mkdir -p $(STAGING_DIR)/usr/include
	$(CP) $(PKG_BUILD_DIR)/* $(STAGING_DIR)/usr/include
	$(CP) $(PKG_BUILD_DIR)/qce50_dlkm.ko $(STAGING_DIR)/usr/include
	$(CP) $(PKG_BUILD_DIR)/qcedev-mod_dlkm.ko $(STAGING_DIR)/usr/include
	$(CP) $(PKG_BUILD_DIR)/qrng_dlkm.ko $(STAGING_DIR)/usr/include
endef

define KernelPackage/$(PKG_NAME)/install
	$(INSTALL_DIR) $(1)/lib/modules
	$(SIGN_KEY) $(PKG_BUILD_DIR)/smcinvoke_dlkm.ko
	$(SIGN_KEY) $(PKG_BUILD_DIR)/tz_log_dlkm.ko
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/smcinvoke_dlkm.ko $(1)/lib/modules/smcinvoke.ko
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/tz_log_dlkm.ko $(1)/lib/modules/tz_log.ko
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/qce50_dlkm.ko $(1)/lib/modules/
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/qcedev-mod_dlkm.ko $(1)/lib/modules/qcedev-mod.ko
	$(INSTALL_BIN) $(PKG_BUILD_DIR)/qrng_dlkm.ko $(1)/lib/modules/msm-rng.ko
	$(INSTALL_DIR) $(1)/etc/init.d
	$(INSTALL_BIN) ./files/tz_log.init $(1)/etc/init.d/
	$(INSTALL_BIN) ./files/smcinvoke.init $(1)/etc/init.d/
	$(INSTALL_BIN) ./files/qrng.init $(1)/etc/init.d/
	$(INSTALL_BIN) ./files/qce50.init $(1)/etc/init.d/
	$(INSTALL_BIN) ./files/qcedev.init $(1)/etc/init.d/
endef

$(eval $(call KernelPackage,$(PKG_NAME)))

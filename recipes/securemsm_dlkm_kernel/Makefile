include $(TOPDIR)/rules.mk
include $(INCLUDE_DIR)/kernel.mk

PKG_NAME:=securemsm_dlkm_kernel
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

$(eval $(call KernelPackage,$(PKG_NAME)))

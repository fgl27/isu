#!/system/bin/sh

# simple service to make shore SU is in place after a reboot that the user has disable it without this no SU after boot

# service example do it on boot to reboot and mv works
#on boot 
#    start isu
#service isu /sbin/isu.sh
#    class main
#    user root
#    seclabel u:r:init:s0
#    disabled
#    oneshot

if [ "$1" = "boot" ]; then
	mount -o rw,remount /system
	if [ -e /system/bin/temp_su ]; then
		rm -rf /system/bin/temp_su
	fi

	if [ -e /system/bin/su ]; then
		rm -rf /system/bin/su
	fi

	if [ ! -e /system/xbin/su ]; then
	su_file=$(cat /data/data/com.bhb27.isu/shared_prefs/pref.xml  | grep cmiyc | cut -d'>' -f2 | cut -d'<' -f1);
		mv /system/xbin/$su_file /system/xbin/su
	# Isu Reboot support end
	fi

	# give su root:root to adb su work optional/recommended
	if [ -e /system/xbin/su ]; then
		chown root:root /system/xbin/su
	fi

	echo 'isu_init: isu init.sh start boot ok' > /dev/kmsg;
	mount -o ro,remount /system;
	umount /system;
# enforce selinux need to pass safety net only apply after boot complete
elif [ "$1" = "boot_completed" ]; then
	echo 'isu_init: isu init.sh start boot_completed ok' > /dev/kmsg;
	/system/bin/log -t isu_init -p i "isu init.sh start boot_completed ok";
	setenforce 1;
fi

exit;


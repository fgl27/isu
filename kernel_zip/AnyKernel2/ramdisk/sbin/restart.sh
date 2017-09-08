#!/system/bin/sh

# simple service to make sure SU is in place after a reboot that the user has disabled it without this no SU after boot

# service example do it on boot to reboot and mv works
#on boot 
#    start restart
#service restart /sbin/restart.sh
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
		mv -f /data/backup_isu /system/xbin/su
        	chmod 0755 /system/xbin/su

		su_daemon=$(getprop persist.sys.root_access);
		if [ $su_daemon -gt 0 ]; then
			if [ $su_daemon -eq 1 ]; then
				start su_daemon
			fi
			if [ $su_daemon -eq 3 ]; then
				start su_daemon
			fi
		fi
	# Isu Reboot support end
	fi

	# give su root:root to adb su work optional/recommended
	if [ -e /system/xbin/su ]; then
		chown root:root /system/xbin/su
	fi

	echo 'isu_init: isu init.sh start boot ok' > /dev/kmsg;
	umount /system;
# enforce selinux need to pass safety net only apply after boot complete
elif [ "$1" = "boot_completed" ]; then
	echo 'isu_init: isu init.sh start boot_completed ok' > /dev/kmsg;
	/system/bin/log -t isu_init -p i "isu init.sh start boot_completed ok";
	setenforce 1;
fi

exit;


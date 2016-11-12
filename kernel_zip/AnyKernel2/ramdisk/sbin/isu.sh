#!/system/bin/sh
# simple service to make shore SU is in place after a reboot that the user has disable it without this no SU after boot

# service example do it on boot to reboot and mv works
#on boot 
#    start isu
#service isu /sbin/isu.sh
#    class main
#    user root
#    disabled
#    oneshot

mount -o rw,remount /system

# Make tmp folder
if [ ! -e /data/tmp ]; then
	mkdir /data/tmp;
	echo "boot start $(date)" > /data/tmp/bootcheck.txt;
else
	echo "boot start $(date)" > /data/tmp/bootcheck.txt;
fi;

# Move bin and xbin back
# Isu support
if [ -e /system/bin/temp_su ]; then
	mv /system/bin/temp_su /system/bin/su
fi

if [ -e /system/xbin/isu ]; then
	mv /system/xbin/isu /system/xbin/su
	if [ ! -e /system/bin/su ]; then
		ln -s -f /system/xbin/su /system/bin/su
	fi
# Isu end
fi

# give su root:root to adb su work optional/recommended
if [ -e /system/xbin/su ]; then
	chown root:root /system/xbin/su
fi

echo "iSu.sh initiated on $(date)" >> /data/tmp/bootcheck.txt
umount /system;
/system/bin/log -t isu_init -p i "isu init.sh start ok"
# enforce selinux need to pass safety net
setenforce 1
exit


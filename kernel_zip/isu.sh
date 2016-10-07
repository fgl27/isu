#!/system/bin/sh
# simple service to make shore SU is in place after a reboot that the user has disable it without this no SU after boot

# service example
#on boot 
#or 
#on property:sys.boot_completed=1
#    start isu
#service isu /sbin/isu.sh
#    class main
#    user root
#    disabled
#    oneshot

mount -o rw,remount /system

# Make tmp folder
if [ -e /data/tmp ]; then
	echo "data/tmp already exist"
else
mkdir /data/tmp
fi

# Move bin and xbin back
if [ -e /system/bin/isu ]; then
	mv /system/bin/isu /system/bin/su
fi

if [ -e /system/xbin/isu ]; then
	mv /system/xbin/isu /system/xbin/su
fi

# give su root:root to adb su work optional/recommended
if [ -e /system/xbin/su ]; then
	chown root:root /system/xbin/su
fi

echo "iSu.sh initiated on $(date)" >> /data/tmp/bootcheck.txt
umount /system;
# optional in case is need to boot in permissive because of this .sh re enable selinux after
#setenforce 1
exit


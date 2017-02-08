# AnyKernel2 Ramdisk Mod Script
# osm0sis @ xda-developers

## AnyKernel setup
# EDIFY properties
kernel.string=iSu by bhb27 @ xda-developers
do.devicecheck=0
do.initd=0
do.modules=0
do.cleanup=1
do.buildprop=0
device.name1=

# shell variables
is_slot_device=0;
# 0 clean kernel support, 1 kernel support, 2 patch default.prop
install_isu=1;
# used by up-bin to print on the default.prop patch
do.isu=0
#Boot in permissive function
dopermissive=1;
#Patch cmline
docmdline=0;
#extra for update-binary
do.cmdline=0
## end setup

## AnyKernel methods (DO NOT CHANGE)
# import patching functions/variables - see for reference
. /tmp/anykernel/ak2-core.sh;

## AnyKernel permissions
# set permissions for included ramdisk files
chmod -R 755 $ramdisk

## AnyKernel install
dump_boot;

if [ $docmdline == 0 ]; then
	if [ $install_isu == 0 ]; then
		# iSu patch remover
		remove_section init.superuser.rc "# isu daemon" "# isu daemon end"
	elif [ $install_isu == 1 ]; then
		# iSu patch include
		if [ -f init.superuser.rc ]; then
		  replace_file init.superuser.rc 750 init.superuser.rc;
		else
		  replace_file init.superuser.rc 750 init.superuser.rc;
		  insert_line init.rc "init.superuser.rc" after "import /init.environ.rc" "import /init.superuser.rc";
		fi;
	elif [ $install_isu == 2 ]; then
		# iSu patch default.prop
		replace_line default.prop "ro.debuggable=1" "ro.debuggable=0"
	fi;
fi;
# end ramdisk changes

write_boot;

## end install

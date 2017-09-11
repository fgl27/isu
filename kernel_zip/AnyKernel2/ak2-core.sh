## AnyKernel methods (DO NOT CHANGE)
# set up extracted files and directories
ramdisk=/tmp/anykernel/ramdisk;
bin=/tmp/anykernel/tools;
split_img=/tmp/anykernel/split_img;
patch=/tmp/anykernel/patch;

chmod -R 755 $bin;
mkdir -p $ramdisk $split_img;


OUTFD=/proc/self/fd/$1;

# ui_print <text>
ui_print() { echo -e "ui_print $1\nui_print" > $OUTFD; }

# contains <string> <substring>
contains() { test "${1#*$2}" != "$1" && return 0 || return 1; }

# dump boot and extract ramdisk
dump_boot() {

  if [ -z "$block" ]; then
    for PARTITION in kern-a KERN-A android_boot ANDROID_BOOT kernel KERNEL boot BOOT lnx LNX; do
      block=$(readlink /dev/block/by-name/$PARTITION || readlink /dev/block/platform/*/by-name/$PARTITION || readlink /dev/block/platform/*/*/by-name/$PARTITION || readlink /dev/block/bootdevice/by-name/$PARTITION)
      if [ ! -z "$block" ]; then break; fi
    done
  fi;

  # recovery.log dump_boot
  if [ -z "$block" ]; then
     if [ -e /tmp/recovery.log ]; then
        block=$(cat /tmp/recovery.log | grep /boot | grep /dev | head -n1 | awk '{print $3;}');
     fi;
  fi;

  # fstab dump_boot
  # Theoretically a fstab exist in root, theoretically must be a /boot, theoretically the partition will be the third part of the string
  if [ -z "$block" ]; then
        block=$(grep boot /*"fstab"* | head -n1 | awk '{print $3;}' );
        # if it doesn't contains mcblk0p readlink
        if [ ! -z "$block" ]; then
           contains $block "mcblk0p" || block=$(readlink $block);
        fi;
  fi;

  if [ "$(getprop ro.product.device)" == "marlin" -o "$(getprop ro.build.product)" == "sailfish" ]; then
    SLOT=$(for i in `cat /proc/cmdline`; do echo $i | grep slot | dd bs=1 skip=24 2>/dev/null; done);
    block="/dev/block/bootdevice/by-name/boot$SLOT";
    ui_print "block $block"; 
  fi;

  if [ -f "$bin/nanddump" ]; then
    $bin/nanddump -f /tmp/anykernel/boot.img $block;
  else
    dd if=$block of=/tmp/anykernel/boot.img;
  fi;
  if [ -f "$bin/unpackelf" ]; then
    $bin/unpackelf -i /tmp/anykernel/boot.img -o $split_img;
    mv -f $split_img/boot.img-ramdisk.cpio.gz $split_img/boot.img-ramdisk.gz;
  else
    $bin/unpackbootimg -i /tmp/anykernel/boot.img -o $split_img;
  fi;
  if [ $? != 0 ]; then
    ui_print "block = $block"; ui_print "Dumping/splitting image failed. Aborting..."; exit 1;
  fi;
  if [ -f "$bin/mkmtkhdr" ]; then
    dd bs=512 skip=1 conv=notrunc if=$split_img/boot.img-ramdisk.gz of=$split_img/temprd;
    mv -f $split_img/temprd $split_img/boot.img-ramdisk.gz;
  fi;
  if [ -f "$bin/unpackelf" -a -f "$split_img/boot.img-dtb" ]; then
    case $(od -ta -An -N4 $split_img/boot.img-dtb | sed -e 's/del //' -e 's/   //g') in
      QCDT|ELF) ;;
      *) gzip $split_img/boot.img-zImage;
         mv -f $split_img/boot.img-zImage.gz $split_img/boot.img-zImage;
         cat $split_img/boot.img-dtb >> $split_img/boot.img-zImage;
         rm -f $split_img/boot.img-dtb;;
    esac;
  fi;
  mv -f $ramdisk /tmp/anykernel/rdtmp;
  mkdir -p $ramdisk;
  magicbytes=$(hexdump -vn2 -e '2/1 "%x"' $split_img/boot.img-ramdisk.gz);
  lzma=0;
  contains $magicbytes "5d0" && lzma=1;
  cd $ramdisk;
  if [ "$lzma" == "1" ]; then
    lzma -dc $split_img/boot.img-ramdisk.gz | cpio -i;
  else
    gunzip -c $split_img/boot.img-ramdisk.gz | cpio -i;
  fi;
  if [ $? != 0 -o -z "$(ls $ramdisk)" ]; then
    ui_print " "; ui_print "Unpacking ramdisk failed. Aborting..."; exit 1;
  fi;
  cp -af /tmp/anykernel/rdtmp/* $ramdisk;
}

# repack ramdisk then build and write image
write_boot() {
  cd $split_img;
  # do permissive
  if [ $dopermissive == 1 ]; then
    sed -ri 's/ enforcing=[0-1]//g' boot.img-cmdline
    sed -ri 's/ androidboot.selinux=permissive|androidboot.selinux=enforcing|androidboot.selinux=disabled//g' boot.img-cmdline
    echo $(cat boot.img-cmdline) androidboot.selinux=permissive > boot.img-cmdline
  fi;
  if [ $docmdline == 1 ]; then
    sed -ri 's/ androidboot.flash.locked=[0-1]//g' boot.img-cmdline
    sed -ri 's/ androidboot.bl_state=[0-2]//g' boot.img-cmdline
    sed -ri 's/ androidboot.verifiedbootstate=green|androidboot.verifiedbootstate=yellow|androidboot.verifiedbootstate=orange|androidboot.verifiedbootstate=red//g' boot.img-cmdline
    sed -ri 's/ buildvariant=eng|buildvariant=user|buildvariant=userdebug//g' boot.img-cmdline
    echo $(cat boot.img-cmdline) buildvariant=user androidboot.verifiedbootstate=green androidboot.bl_state=0 androidboot.flash.locked=1 > boot.img-cmdline
  fi;
  if [ -f *-cmdline ]; then
    cmdline=`cat *-cmdline`;
  fi;
  if [ -f *-board ]; then
    board=`cat *-board`;
  fi;
  base=`cat *-base`;
  pagesize=`cat *-pagesize`;
  kerneloff=`cat *-kerneloff`;
  ramdiskoff=`cat *-ramdiskoff`;
  if [ -f *-tagsoff ]; then
    tagsoff=`cat *-tagsoff`;
  fi;
  if [ -f *-osversion ]; then
    osver=`cat *-osversion`;
  fi;
  if [ -f *-oslevel ]; then
    oslvl=`cat *-oslevel`;
  fi;
  if [ -f *-second ]; then
    second=`ls *-second`;
    second="--second $split_img/$second";
    secondoff=`cat *-secondoff`;
    secondoff="--second_offset $secondoff";
  fi;
  for i in zImage zImage-dtb Image.gz Image.gz-dtb; do
    if [ -f /tmp/anykernel/$i ]; then
      kernel=/tmp/anykernel/$i;
      break;
    fi;
  done;
  if [ ! "$kernel" ]; then
    kernel=`ls *-zImage`;
    kernel=$split_img/$kernel;
  fi;
  if [ -f /tmp/anykernel/dtb ]; then
    dtb="--dt /tmp/anykernel/dtb";
  elif [ -f *-dtb ]; then
    dtb=`ls *-dtb`;
    dtb="--dt $split_img/$dtb";
  fi;
  if [ -f "$bin/mkbootfs" ]; then
    $bin/mkbootfs /tmp/anykernel/ramdisk | gzip > /tmp/anykernel/ramdisk-new.cpio.gz;
  else
    cd $ramdisk;
    # re compress ramdisk using gz even if it was compressed with lzma in the first place, hoping not to break any boot.img max size
    find . | cpio -H newc -o | gzip > /tmp/anykernel/ramdisk-new.cpio.gz;
  fi;
  if [ $? != 0 ]; then
    ui_print " "; ui_print "Repacking ramdisk failed. Aborting..."; exit 1;
  fi;
  if [ -f "$bin/mkmtkhdr" ]; then
    cd /tmp/anykernel;
    $bin/mkmtkhdr --rootfs ramdisk-new.cpio.gz;
    mv -f ramdisk-new.cpio.gz-mtk ramdisk-new.cpio.gz;
    case $kernel in
      $split_img/*) ;;
      *) $bin/mkmtkhdr --kernel $kernel; kernel=$kernel-mtk;;
    esac;
  fi;
  $bin/mkbootimg --kernel $kernel --ramdisk /tmp/anykernel/ramdisk-new.cpio.gz $second --cmdline "$cmdline" --board "$board" --base $base --pagesize $pagesize --kernel_offset $kerneloff --ramdisk_offset $ramdiskoff $secondoff --tags_offset "$tagsoff" --os_version "$osver" --os_patch_level "$oslvl" $dtb --output /tmp/anykernel/boot-new.img;
  if [ $? != 0 ]; then
    ui_print " "; ui_print "Repacking image failed. Aborting..."; exit 1;
  elif [ `wc -c < /tmp/anykernel/boot-new.img` -gt `wc -c < /tmp/anykernel/boot.img` ]; then
    ui_print " "; ui_print "New image larger than boot partition. Aborting..."; exit 1;
  fi;
  if [ -f "$bin/BootSignature_Android.jar" -a -d "$bin/avb" ]; then
    if [ -f "/system/system/bin/dalvikvm" ]; then
      umount /system;
      umount /system 2>/dev/null;
      mkdir /system_root;
      mount -o ro -t auto /dev/block/bootdevice/by-name/system$slot /system_root;
      mount -o bind /system_root/system /system;
    fi;
    unset LD_LIBRARY_PATH;
    pk8=`ls $bin/avb/*.pk8`;
    cert=`ls $bin/avb/*.x509.*`;
    /system/bin/dalvikvm -Xbootclasspath:/system/framework/core-oj.jar:/system/framework/core-libart.jar:/system/framework/conscrypt.jar:/system/framework/bouncycastle.jar -Xnodex2oat -Xnoimage-dex2oat -cp $bin/BootSignature_Android.jar com.android.verity.BootSignature /boot boot-new.img $pk8 $cert boot-new-signed.img;
    if [ $? != 0 ]; then
      ui_print " "; ui_print "Signing image failed. Aborting..."; exit 1;
    fi;
    mv -f boot-new-signed.img boot-new.img;
    if [ -d "/system_root" ]; then
      umount /system;
      umount /system_root;
      rmdir /system_root;
      mount -o ro -t auto /system;
    fi;
  fi;
  if [ -f "$bin/blobpack" ]; then
    printf '-SIGNED-BY-SIGNBLOB-\00\00\00\00\00\00\00\00' > boot-new-signed.img;
    $bin/blobpack tempblob LNX boot-new.img;
    cat tempblob >> boot-new-signed.img;
    mv -f boot-new-signed.img boot-new.img;
  fi;
  if [ -f "/data/custom_boot_image_patch.sh" ]; then
    ash /data/custom_boot_image_patch.sh /tmp/anykernel/boot-new.img;
    if [ $? != 0 ]; then
      ui_print " "; ui_print "User script execution failed. Aborting..."; exit 1;
    fi;
  fi;
  # bump image specific for lg devices
lg=0
listLG2="d800
d801
d802
d803
f320
l01f
ls980
vs980
";

listLG3="d850
d851
d852
d855
f400
ls990
vs985
";
  for i in $listLG2;
  do
    if [ "$(getprop ro.product.device)" == "$i" -o "$(getprop ro.build.product)" == "$i" ]; then
      ui_print "Device $i";
      lg=2;
    fi;
  done;
  for i in $listLG3;
  do
    if [ "$(getprop ro.product.device)" == "$i" -o "$(getprop ro.build.product)" == "$i" ]; then
      ui_print "Device $i";
      lg=3;
    fi;
  done;

  ui_print " ";
  if [ "$lg" == 2 ]; then
    ui_print "bump image specific for LG G2";
    dd if=$bin/bump >> /tmp/anykernel/boot-new.img;
  fi;
  if [ "$lg" == 3 ]; then
    ui_print "bump image specific for LG G3";
    dd if=$bin/bump bs=1 count=32 >> /tmp/anykernel/boot-new.img;
    dd if=/dev/zero of=$block;
  fi;
  if [ "$(strings /tmp/anykernel/boot.img | grep SEANDROIDENFORCE )" ]; then
    printf 'SEANDROIDENFORCE' >> /tmp/anykernel/boot-new.img;
  fi;
  if [ -f "$bin/flash_erase" -a -f "$bin/nandwrite" ]; then
    $bin/flash_erase $block 0 0;
    $bin/nandwrite -p $block /tmp/anykernel/boot-new.img;
  else
    dd if=/dev/zero of=$block;
    dd if=/tmp/anykernel/boot-new.img of=$block;
  fi;
}

# backup_file <file>
backup_file() { test ! -f $1~ && cp $1 $1~; }

# replace_string <file> <if search string> <original string> <replacement string>
replace_string() {
  if [ -z "$(grep "$2" $1)" ]; then
      sed -i "s;${3};${4};" $1;
  fi;
}

# replace_section <file> <begin search string> <end search string> <replacement string>
replace_section() {
  begin=`grep -n "$2" $1 | head -n1 | cut -d: -f1`;
  for end in `grep -n "$3" $1 | cut -d: -f1`; do
    if [ "$begin" -lt "$end" ]; then
      if [ "$3" == " " ]; then
        sed -i "/${2//\//\\/}/,/^\s*$/d" $1;
      else
        sed -i "/${2//\//\\/}/,/${3//\//\\/}/d" $1;
      fi;
      sed -i "${begin}s;^;${4}\n;" $1;
      break;
    fi;
  done;
}

# remove_section <file> <begin search string> <end search string>
remove_section() {
  begin=`grep -n "$2" $1 | head -n1 | cut -d: -f1`;
  for end in `grep -n "$3" $1 | cut -d: -f1`; do
    if [ "$begin" -lt "$end" ]; then
      if [ "$3" == " " ]; then
        sed -i "/${2//\//\\/}/,/^\s*$/d" $1;
      else
        sed -i "/${2//\//\\/}/,/${3//\//\\/}/d" $1;
      fi;
      break;
    fi;
  done;
}

# insert_line <file> <if search string> <before|after> <line match string> <inserted line>
insert_line() {
  if [ -z "$(grep "$2" $1)" ]; then
    case $3 in
      before) offset=0;;
      after) offset=1;;
    esac;
    line=$((`grep -n "$4" $1 | head -n1 | cut -d: -f1` + offset));
    sed -i "${line}s;^;${5}\n;" $1;
  fi;
}

# replace_line <file> <line replace string> <replacement line>
replace_line() {
  if [ ! -z "$(grep "$2" $1)" ]; then
    line=`grep -n "$2" $1 | head -n1 | cut -d: -f1`;
    sed -i "${line}s;.*;${3};" $1;
  fi;
}

# remove_line <file> <line match string>
remove_line() {
  if [ ! -z "$(grep "$2" $1)" ]; then
    line=`grep -n "$2" $1 | head -n1 | cut -d: -f1`;
    sed -i "${line}d" $1;
  fi;
}

# prepend_file <file> <if search string> <patch file>
prepend_file() {
  if [ -z "$(grep "$2" $1)" ]; then
    echo "$(cat $patch/$3 $1)" > $1;
  fi;
}

# insert_file <file> <if search string> <before|after> <line match string> <patch file>
insert_file() {
  if [ -z "$(grep "$2" $1)" ]; then
    case $3 in
      before) offset=0;;
      after) offset=1;;
    esac;
    line=$((`grep -n "$4" $1 | head -n1 | cut -d: -f1` + offset));
    sed -i "${line}s;^;\n;" $1;
    sed -i "$((line - 1))r $patch/$5" $1;
  fi;
}

# append_file <file> <if search string> <patch file>
append_file() {
  if [ -z "$(grep "$2" $1)" ]; then
    echo -ne "\n" >> $1;
    cat $patch/$3 >> $1;
    echo -ne "\n" >> $1;
  fi;
}

# replace_file <file> <permissions> <patch file>
replace_file() {
  cp -pf $patch/$3 $1;
  chmod $2 $1;
}

# patch_fstab <fstab file> <mount match name> <fs match type> <block|mount|fstype|options|flags> <original string> <replacement string>
patch_fstab() {
  entry=$(grep "$2" $1 | grep "$3");
  if [ -z "$(echo "$entry" | grep "$6")" ]; then
    case $4 in
      block) part=$(echo "$entry" | awk '{ print $1 }');;
      mount) part=$(echo "$entry" | awk '{ print $2 }');;
      fstype) part=$(echo "$entry" | awk '{ print $3 }');;
      options) part=$(echo "$entry" | awk '{ print $4 }');;
      flags) part=$(echo "$entry" | awk '{ print $5 }');;
    esac;
    newentry=$(echo "$entry" | sed "s;${part};${6};");
    sed -i "s;${entry};${newentry};" $1;
  fi;
}

## end methods

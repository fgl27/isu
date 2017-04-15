#!/bin/bash
# simple build sh to build a apk check folder and sign ...set on yours .bashrc to call this sh from anywhere alias bt='/home/user/this.sh'

#timer counter
START=$(date +%s.%N);
START2="$(date)";
echo -e "\n Script start $(date)\n";

#Folders Folder= you app folder SDK_Folder android sdk folder Download it if you don't have it, don't remove the sdk.dir= from the line

FOLDER=$HOME/android/isu;
SDK_FOLDER="sdk.dir=$HOME/android/sdk";

# Export Java path in some machines is necessary put your java path

#export JAVA_HOME="/usr/lib/jvm/java-7-openjdk-amd64/"

#Generate and use a sign key https://developer.android.com/studio/publish/app-signing.html
#keytool -genkey -v -keystore key_name.key -alias <chose_a_alias> -keyalg RSA -keysize 2048 -validity 10000
#sign with
#jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass <yours_password> -keystore <file_path.apk> <new_file_path.apk> <chose_a_alias>
#check
# jarsigner -verify -verbose -certs <my_application.apk>
SIGN=1;
ZIPALIGN_FOLDER=$HOME/android/sdk/build-tools/25.0.2/zipalign;
KEY_FOLDER=$HOME/android/temp/sign/fgl.key;
KEY_PASS=$(</$HOME/android/temp/sign/pass);

# out app folder and out app name
VERSION=$(grep versionName $FOLDER/app/build.gradle | head -n1 | cut -d\" -f2 | sed 's/\./_/');
OUT_FOLDER=$FOLDER/app/build/outputs/apk;
APP_FINAL_NAME=iSu_$VERSION.apk;

# make zip only used if you have the need to make a zip of this a flash zip template is need
# Auto sign zip Download from my folder link below extract and set the folder below on yours machine
# https://www.androidfilehost.com/?fid=312978532265364585
# ZIPAPPFOLDER = folder of the zip the contains the apk inside the zip
MKZIP=1;
ANYKERNEL=$FOLDER/kernel_zip/AnyKernel2/;
ZIP_SIGN_FOLDER=$HOME/android/ZipScriptSign;
ZIPNAME_ENFORCE=iSu_kernel_Reboot_Support_V_$VERSION\_and_up_Enforcing;
ZIPNAME_PERMISSIVE=iSu_kernel_Reboot_Support_V_$VERSION\_and_up_Permissive;
ZIPNAME_REBOOT=iSu_kernel_Reboot_Support_V_$VERSION\_and_up;
ZIPNAME_CMDLINE=iSu_kernel_cmdline_Patch_V_$VERSION\_and_up;
ZIPNAME_PROP=iSu_kernel_defaultprop_Patch_V_$VERSION\_and_up;
ZIPNAME_PIXEL=iSu_kernel_Pixel_Patch_V_$VERSION\_and_up;
#making start here...

cd $FOLDER;

if [ ! -e ./local.properties ]; then
	echo -e "$\n local.properties not found...\nMaking a local.properties files using script information\n
\n local.properties done starting the build";
	touch $FOLDER.local.properties;
	echo $SDK_FOLDER > local.properties;
fi;
localproperties=`cat local.properties`;
if [ $localproperties != $SDK_FOLDER ]; then
	echo -e "\nSDK folder set as \n$SDK_FOLDER in the script \nbut local.properties file content is\n$localproperties\nfix it using script value";
	rm -rf .local.properties;
	touch $FOLDER.local.properties;
	echo $SDK_FOLDER > local.properties;
fi;

./gradlew clean
echo -e "\n The above is just the cleaning build start now\n";
./gradlew build

if [ $SIGN == 1 ]; then
if [ ! -e ./app/build/outputs/apk/app-release-unsigned.apk ]; then
	echo -e "\n${bldred}App not build${txtrst}\n"
	exit 1;
else
	jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass $KEY_PASS -keystore $KEY_FOLDER $OUT_FOLDER/app-release-unsigned.apk Felipe_Leon
	$ZIPALIGN_FOLDER -v 4 $OUT_FOLDER/app-release-unsigned.apk $OUT_FOLDER/$APP_FINAL_NAME
	cp $OUT_FOLDER/$APP_FINAL_NAME $OUT_FOLDER/isu$(date +%s).apk
fi;
fi;

if [ $MKZIP == 1 ]; then
	echo -e "\nMaking the zips\n"

	echo -e "\nKernel reboot support enforce\n"
	cd $ANYKERNEL/
	rm -rf *.zip
	zip -r9 $ZIPNAME_ENFORCE * -x README .gitignore *.zip tools/su*
	$ZIP_SIGN_FOLDER/sign.sh test  $ANYKERNEL/$ZIPNAME_ENFORCE.zip
	rm -rf ./ZipScriptSign/$ZIPNAME_ENFORCE.zip
	mv $ANYKERNEL/$ZIPNAME_ENFORCE-signed.zip $ANYKERNEL/$ZIPNAME_ENFORCE.zip

	echo -e "\nKernel reboot support permissive\n"
	sed -i '/	setenforce 1/c\	setenforce 0\;' $ANYKERNEL/ramdisk/sbin/restart.sh;
	zip -r9 $ZIPNAME_PERMISSIVE * -x README .gitignore *.zip tools/su*
	$ZIP_SIGN_FOLDER/sign.sh test  $ANYKERNEL/$ZIPNAME_PERMISSIVE.zip
	rm -rf ./ZipScriptSign/$ZIPNAME_PERMISSIVE.zip
	mv $ANYKERNEL/$ZIPNAME_PERMISSIVE-signed.zip $ANYKERNEL/$ZIPNAME_PERMISSIVE.zip

	echo -e "\ncleaning sed\n"
	sed -i '/	setenforce 0/c\	setenforce 1\;' $ANYKERNEL/ramdisk/sbin/restart.sh;
fi;

END2="$(date)";
END=$(date +%s.%N);
echo -e "\nScript start $START2";
echo -e "End $END2 \n";
echo -e "\n${bldgrn}Total time elapsed of the script: ${txtrst}${grn}$(echo "($END - $START) / 60"|bc ):$(echo "(($END - $START) - (($END - $START) / 60) * 60)"|bc ) (minutes:seconds). ${txtrst}\n";


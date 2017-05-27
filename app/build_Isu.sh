#!/bin/bash
#timer counter
START=$(date +%s.%N);
START2="$(date)";
echo -e "\n Script start $(date)\n";

# simple build sh to build a apk check folder and sign ...set on yours .bashrc to call this sh from anywhere alias bt='/home/user/this.sh'
# How to build it set the below path/folders and install java and Download android sdk, setup a key to sigh or set SIGN to 0 and use debug.apk
# the rest must work without problems

# Folders Folder= yours app main folder, SDK_FOLDER android sdk folder
FOLDER="$HOME"/android/isu;
SDK_FOLDER="$HOME"/android/sdk;
SDK_DIR="sdk.dir=$SDK_FOLDER";

# app sign key
#Generate and use a sign key https://developer.android.com/studio/publish/app-signing.html
#keytool -genkey -v -keystore key_name.key -alias <chose_a_alias> -keyalg RSA -keysize 2048 -validity 10000
#sign with
#jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass <yours_password> -keystore <file_path.apk> <new_file_path.apk> <chose_a_alias>
#check
# jarsigner -verify -verbose -certs <my_application.apk>
SIGN=1;
KEY_FOLDER="$HOME"/android/temp/sign/fgl.key;
KEY_PASS=$(</"$HOME"/android/temp/sign/pass);

# make kernel_zip
MKZIP=0;
# make zip only used if you have the need to make a zip of this a flash zip template is need
# Auto sign zip Download from my folder link below extract and set the folder below on yours machine
# https://www.androidfilehost.com/?fid=312978532265364585
ZIP_SIGN_FOLDER="$HOME"/android/ZipScriptSign;

#Bellow this line theoretically noting need to be changed

# sdk tool and zipzlign path
TOOLVERSION=$(grep buildToolsVersion "$FOLDER"/app/build.gradle | head -n1 | cut -d\" -f2);
ZIPALIGN_FOLDER=$SDK_FOLDER/build-tools/$TOOLVERSION/zipalign;

# out app folder and out app name
VERSION=$(grep versionName "$FOLDER"/app/build.gradle | head -n1 | cut -d\" -f2 | sed 's/\./_/');
OUT_FOLDER="$FOLDER"/app/build/outputs/apk;
APP_FINAL_NAME=iSu_$VERSION.apk;

#kernel_zip
ANYKERNEL="$FOLDER"/kernel_zip/AnyKernel2/;
ZIPNAME_ENFORCE=iSu_kernel_Reboot_Support_V_"$VERSION"_and_up_Enforcing;
ZIPNAME_PERMISSIVE=iSu_kernel_Reboot_Support_V_"$VERSION"_and_up_Permissive;

#making start here...

cd "$FOLDER" || exit;

if [ ! -e ./local.properties ]; then
	echo -e "$\n local.properties not found...\nMaking a local.properties files using script information\n
\n local.properties done starting the build";
	touch "$FOLDER".local.properties;
	echo "$SDK_DIR" > local.properties;
fi;
localproperties=$(cat < local.properties | head -n1);
if [ "$localproperties" != "$SDK_DIR" ]; then
	echo -e "\nSDK folder set as \n$SDK_DIR in the script \nbut local.properties file content is\n$localproperties\nfix it using script value";
	rm -rf .local.properties;
	touch "$FOLDER".local.properties;
	echo "$SDK_DIR" > local.properties;
fi;

./gradlew clean
echo -e "\n The above is just the cleaning build start now\n";
rm -rf app/build/outputs/apk/**
./gradlew build 2>&1 | tee build_log.txt

if [ ! -e ./app/build/outputs/apk/app-release-unsigned.apk ]; then
	echo -e "\nApp not build$\n"
	exit 1;
elif [ $SIGN == 1 ]; then
	jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass "$KEY_PASS" -keystore "$KEY_FOLDER" "$OUT_FOLDER"/app-release-unsigned.apk Felipe_Leon
	"$ZIPALIGN_FOLDER" -v 4 "$OUT_FOLDER"/app-release-unsigned.apk "$OUT_FOLDER"/"$APP_FINAL_NAME"
	cp "$OUT_FOLDER"/"$APP_FINAL_NAME" "$OUT_FOLDER"/isu"$(date +%s)".apk
fi;

if [ $MKZIP == 1 ]; then
	echo -e "\nMaking the zips\n"

	echo -e "\nKernel reboot support enforce\n"
	cd "$ANYKERNEL"/ || exit
	rm -rf ./**.zip
	zip -r9 "$ZIPNAME_ENFORCE" ./** -x README .gitignore ./**.zip tools/su*
	"$ZIP_SIGN_FOLDER"/sign.sh test  "$ANYKERNEL"/"$ZIPNAME_ENFORCE".zip
	rm -rf ./ZipScriptSign/"$ZIPNAME_ENFORCE".zip
	mv "$ANYKERNEL"/"$ZIPNAME_ENFORCE"-signed.zip "$ANYKERNEL"/"$ZIPNAME_ENFORCE".zip

	echo -e "\nKernel reboot support permissive\n"
	sed -i '/	setenforce 1/c\	setenforce 0\;' "$ANYKERNEL"/ramdisk/sbin/restart.sh;
	zip -r9 "$ZIPNAME_PERMISSIVE" ./** -x README .gitignore ./**.zip tools/su*
	"$ZIP_SIGN_FOLDER"/sign.sh test  "$ANYKERNEL"/"$ZIPNAME_PERMISSIVE".zip
	rm -rf ./ZipScriptSign/"$ZIPNAME_PERMISSIVE".zip
	mv "$ANYKERNEL"/"$ZIPNAME_PERMISSIVE"-signed.zip "$ANYKERNEL"/"$ZIPNAME_PERMISSIVE".zip

	echo -e "\ncleaning sed\n"
	sed -i '/	setenforce 0/c\	setenforce 1\;' "$ANYKERNEL"/ramdisk/sbin/restart.sh;
fi;

END2="$(date)";
END=$(date +%s.%N);

if [ -e "$OUT_FOLDER"/"$APP_FINAL_NAME" ]; then
	echo -e "\nLint issues:\n";
	grep issues build_log.txt;

	echo -e "\nApp saved at $OUT_FOLDER"/"$APP_FINAL_NAME\n"
fi;
rm -rf build_log.txt
echo -e "*** Build END ***"
echo -e "\nTotal elapsed time of the script: $(echo "($END - $START) / 60"|bc ):$(echo "(($END - $START) - (($END - $START) / 60) * 60)"|bc ) (minutes:seconds).\n";
exit 1;

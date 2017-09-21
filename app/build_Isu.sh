#!/bin/bash
#colors
RED='\033[1;31m'
CYAN='\033[1;36m' 
GREEN='\033[1;32m' 
YELLOW='\033[1;33m' 
NC='\033[1m'
#timer counter
START=$(date +%s.%N);
START2="$(date)";
echo -e "\n Script start $(date)\n";

# simple build sh to build a apk check folder and sign ...set on yours .bashrc to call this sh from anywhere alias bt='/home/user/this.sh'
# How to build it set the below path/folders and install java and Download android sdk, setup a key to sigh or set SIGN to 0 and use debug.apk
# the rest must work without problems

# Folders Folder= yours app main folder, SDK_FOLDER android sdk folder
#ndk lincense fix go to folder sdk/tool/bin and ./sdkmanager --update
#ndk download https://developer.android.com/ndk/downloads/index.html extrac to $NDK_DIR
FOLDER="$HOME"/android/isu;
SDK_FOLDER="$HOME"/android/sdk;
SDK_DIR="sdk.dir=$SDK_FOLDER";
NDK_DIR="ndk.dir=$SDK_FOLDER/ndk";

# app sign key
#Generate and use a sign key https://developer.android.com/studio/publish/app-signing.html
#keytool -genkey -v -keystore key_name.key -alias <chose_a_alias> -keyalg RSA -keysize 2048 -validity 10000
#sign with
#jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass <yours_password> -keystore <file_path.apk> <new_file_path.apk> <chose_a_alias>
#check
# jarsigner -verify -verbose -certs <my_application.apk>
# for play auto sign tool
#java -jar pepk.jar --keystore=fgl.key --alias=felipe_leon --output=fgl.keystore --encryptionkey=eb10fe8f7c7c9df715022017b00c6471f8ba8170b13049a11e6c09ffe3056a104a3bbe4ac5a955f4ba4fe93fc8cef27558a3eb9d2a529a2092761fb833b656cd48b9de6a
#keytool -genkey -v -keystore fgl_pem.key -alias felipe_leon -keyalg RSA -keysize 2048 -validity 10000
#
#keytool -export -rfc -keystore fgl_pem.key -alias felipe_leon -file fgl_pem.pem
SIGN=1;
KEY_FOLDER="$HOME"/android/temp/sign/fgl.key;
KEY_PASS=$(</"$HOME"/android/temp/sign/pass);

#build the app BAPP=1 or kernel_zip BAPP=0?
BAPP=1;
# make zip only used if you have the need to make a zip of this a flash zip template is need
# Auto sign zip Download from my folder link below extract and set the folder below on yours machine
# https://www.androidfilehost.com/?fid=312978532265364585
ZIP_SIGN_FOLDER="$HOME"/android/ZipScriptSign;

#Bellow this line theoretically noting need to be changed

# sdk tool and zipzlign path
TOOLVERSION=$(grep buildTools "$FOLDER"/versions.gradle | head -n1 | cut -d\' -f2);
ZIPALIGN_FOLDER=$SDK_FOLDER/build-tools/$TOOLVERSION/zipalign;

# out app folder and out app name
VERSION=$(grep publishVersion "$FOLDER"/versions.gradle | head -n1 | cut -d\' -f2 | sed 's/\./_/');
OUT_FOLDER="$FOLDER"/app/build/outputs/apk/release;
APP_FINAL_NAME=iSu_$VERSION.apk;

#kernel_zip
ANYKERNEL="$FOLDER"/kernel_zip/AnyKernel2/;
ZIPNAME_UNIVERSAL=iSu_kernel_Reboot_Support_V_"$VERSION"_and_up;
ZIPNAME_ENFORCE=iSu_kernel_Reboot_Support_V_"$VERSION"_and_up_Enforcing;
ZIPNAME_PERMISSIVE=iSu_kernel_Reboot_Support_V_"$VERSION"_and_up_Permissive;

#making start here...
contains() {
    string="$1"
    substring="$2"
    if test "${string#*$substring}" != "$string"
    then
        return 0    # $substring is in $string
    else
        return 1    # $substring is not in $string
    fi
}

cd "$FOLDER" || exit;

if [ ! -e ./local.properties ]; then
	echo -e "$\n local.properties not found...\nMaking a local.properties files using script information\n
\n local.properties done starting the build";
	touch "$FOLDER".local.properties;
	echo "$SDK_DIR" > local.properties;
	echo "$NDK_DIR" >> local.properties;
fi;

localproperties=$(echo $(cat local.properties));
localOK=0;
contains "$localproperties" "$NDK_DIR" && contains "$localproperties" "$SDK_DIR" && localOK=1;

if [ "$localOK" == 0 ]; then
	echo -e "\nSDK folder set as \n$SDK_DIR in the script \nbut local.properties file content is\n$localproperties\nfix it using script value";
	rm -rf .local.properties;
	touch "$FOLDER".local.properties;
	echo "$SDK_DIR" > local.properties;
	echo "$NDK_DIR" >> local.properties;
fi;

if [ $BAPP == 1 ]; then
	./gradlew clean
	echo -e "\n The above is just the cleaning build start now\n";
	rm -rf app/build/outputs/apk/**
	./gradlew build 2>&1 | tee build_log.txt

	if [ ! -e ./app/build/outputs/apk/release/app-release-unsigned.apk ]; then
		echo -e "\n${RED}App not buil!\n${NC}";
		exit 1;
	elif [ $SIGN == 1 ]; then
		jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -storepass "$KEY_PASS" -keystore "$KEY_FOLDER" "$OUT_FOLDER"/app-release-unsigned.apk Felipe_Leon
		"$ZIPALIGN_FOLDER" -v 4 "$OUT_FOLDER"/app-release-unsigned.apk "$OUT_FOLDER"/"$APP_FINAL_NAME"
		cp "$OUT_FOLDER"/"$APP_FINAL_NAME" "$OUT_FOLDER"/isu"$(date +%s)".apk
	fi;
fi;

if [ $BAPP == 0 ]; then
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

	echo -e "\nKernel reboot support Universal\n"
	sed -i '/	setenforce 0/c\	#setenforce 1\;' "$ANYKERNEL"/ramdisk/sbin/restart.sh;
	sed -i '/dopermissive=1/c\dopermissive=0\;' "$ANYKERNEL"/anykernel.sh;
	cd "$ANYKERNEL"/ || exit
	zip -r9 "$ZIPNAME_UNIVERSAL" ./** -x README .gitignore ./**.zip tools/su*
	"$ZIP_SIGN_FOLDER"/sign.sh test  "$ANYKERNEL"/"$ZIPNAME_UNIVERSAL".zip
	rm -rf ./ZipScriptSign/"$ZIPNAME_UNIVERSAL".zip
	mv "$ANYKERNEL"/"$ZIPNAME_UNIVERSAL"-signed.zip "$ANYKERNEL"/"$ZIPNAME_UNIVERSAL".zip

	echo -e "\ncleaning sed\n"
	sed -i '/	#setenforce 1/c\	setenforce 1\;' "$ANYKERNEL"/ramdisk/sbin/restart.sh;
	sed -i '/dopermissive=0/c\dopermissive=1\;' "$ANYKERNEL"/anykernel.sh;
	cd "$FOLDER" || exit;
fi;

END2="$(date)";
END=$(date +%s.%N);

#Build issues, deprecations and updates checker
if [ -e "$OUT_FOLDER"/"$APP_FINAL_NAME" ]; then
	echo "$(./gradlew -q gradleUpdates | sed '/jacoco/d')" >> build_log.txt

        ISSUES=$(grep issues build_log.txt | grep release)
	if [ -n "$ISSUES" ]; then
		NOISSUES=0;
		contains "$ISSUES" ": 0 issues" && NOISSUES=1;
		if [ $NOISSUES == 0 ]; then
			echo -e "\n${CYAN}Lint issues:\n${NC}";
			echo -e "${RED}$ISSUES${NC}";
			sensible-browser "$FOLDER"/app/build/reports/lint-results.html
		fi;
	fi;

        DEPRECATION=$(grep deprecation build_log.txt)
	if [ -n "$DEPRECATION" ]; then
		echo -e "\n${CYAN}Build deprecation:\n${NC}";
		echo -e "${RED}$DEPRECATION${NC}";
	fi;

        UPDATEDEPENDENCIES=$(grep '\->' build_log.txt)
	if [ -n "$UPDATEDEPENDENCIES" ]; then
		echo -e "\n${CYAN}Dependencies that need update:\n${NC}";
		echo -e "${RED}$UPDATEDEPENDENCIES${NC}";
	fi;

        GRADLEVERSION=$(grep distributionUrl ./gradle/wrapper/gradle-wrapper.properties | head -n1 | cut -d\/ -f5)
        LASTGRADLEVERSION=$(grep 'current version' build_log.txt  | head -n1 | cut -d\/ -f5| cut -d\) -f1)
        LASTRCGRADLEVERSION=$(grep 'release-candidat' build_log.txt  | head -n1 | cut -d\/ -f5| cut -d\) -f1)
        NORC=1;
	contains "$LASTRCGRADLEVERSION" "null" && NORC=0;
	if [ $NORC == 1 ]; then
		if [ ! "$GRADLEVERSION" == "$LASTRCGRADLEVERSION" ]; then
			echo -e "\n${CYAN}Gradlew RC need update:\n${NC}";
			echo -e "\n${RED}current $GRADLEVERSION latest RC $LASTRCGRADLEVERSION\n${NC}";
		fi;
	elif [ ! "$GRADLEVERSION" == "$LASTGRADLEVERSION" ]; then
		echo -e "\n${CYAN}Gradlew need update:\n${NC}";
		echo -e "\n${RED}Current $GRADLEVERSION latest $LASTGRADLEVERSION\n${NC}";
	fi;

	echo -e "\n${GREEN}App saved at $OUT_FOLDER"/"$APP_FINAL_NAME${NC}";
fi;
echo -e "\n${YELLOW}*** Build END ***\n";
echo -e "Total elapsed time of the script: ${RED}$(echo "($END - $START) / 60"|bc ):$(echo "(($END - $START) - (($END - $START) / 60) * 60)"|bc ) ${YELLOW}(minutes:seconds).\n${NC}";
exit 1;

# iSu

A simple app to hide SU and pass safety net verification, today it works only on SU binary of CM base ROM's

![isu_A01_Screenshot](https://raw.githubusercontent.com/bhb27/isu/master/screenshots/A01.png)
![isu_A02_Screenshot](https://raw.githubusercontent.com/bhb27/isu/master/screenshots/A02.png)

## Report a bug request device support

You can report a bug or a request by [opening an issue](https://github.com/bhb27/isu/issues/new) or
mention @bhb27 with the request/bug in [XDA support forum](http://forum.xda-developers.com/android/apps-games/isu-simple-app-to-deactivate-activate-t3478348)

## Download

 [Download Link](https://www.androidfilehost.com/?w=files&flid=120360)

## Changelog

 [iSu Changelog](https://github.com/bhb27/isu/wiki/Changelog)

## XDA

 [XDA support forum](http://forum.xda-developers.com/android/apps-games/isu-simple-app-to-deactivate-activate-t3478348)

## Build

Clone the project and come in:

``` bash
$ git clone git://github.com/bhb27/isu.git
$ cd isu_folder_path
$ ./gradlew build
```

I particularly prefer to use this .sh to build
 [iSu Build Script](https://github.com/bhb27/isu/blob/master/app/build_Isu.sh)

## License

    Copyright (C) 2016 Felipe de Leon <fglfgl27@gmail.com>
    
    iSu is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    iSu is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with iSu.  If not, see <http://www.gnu.org/licenses/>.

## Credits

* ROOT function of Kernel adiutor original 2015 version [by Grarak](https://github.com/Grarak/KernelAdiutor)
* For the Kernel_reboot_support was used AnyKernel2 as base [by AnyKernel2](https://github.com/osm0sis/AnyKernel2)
* Appcompat v7 Library [by google](https://developer.android.com/topic/libraries/support-library/packages.html#v7-appcompat)
* Busybox Binary, the Swiss Army Knife of Embedded Linux [by Denys Vlasenko, build by Chris Renshaw aka osm0sis](https://forum.xda-developers.com/showthread.php?t=2239421)
* Design Library [by google](https://developer.android.com/topic/libraries/support-library/packages.html#design)
* MagiskPolicy Binary, sepolicy injection utility [by John Wu aka topjohnwu](https://github.com/topjohnwu/Magisk#credits)
* Material Dialogs Library, a beautiful, fluid, and customizable dialogs API [by Aidan Follestad](https://github.com/afollestad/material-dialogs)
* okhttp Library, HTTP+HTTP/2 client for Android and Java applications [by Square](https://github.com/square/okhttp)
* Play Services Safetynet Library [by google](https://developers.google.com/android/guides/setup#ensure_devices_have_the_google_play_services_apk)
* Preference v14 Library [by google](https://developer.android.com/topic/libraries/support-library/packages.html#v14-preference)
* Resetprop Binary, to Manipulate any system props [by John Wu aka topjohnwu](https://github.com/topjohnwu/Magisk#credits)
* Support v4 Library [by google](https://developer.android.com/topic/libraries/support-library/packages.html#v4)
* Support v13 Library [by google](https://developer.android.com/topic/libraries/support-library/packages.html#v13)
* ZeroTurnaround ZIP Library [by zeroturnaround](https://github.com/zeroturnaround/zt-zip)
* Gradle Versions Plugin [by ben-manes](https://github.com/ben-manes/gradle-versions-plugin#gradle-versions-plugin)
* Bouncy Castle Crypto APIs [by bouncycastle.org](https://www.bouncycastle.org/)
* Zipadjust Library [From MagiskManager](https://github.com/topjohnwu/MagiskManager/tree/master/app/src/main/jni)


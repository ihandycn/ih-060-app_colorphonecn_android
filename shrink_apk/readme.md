# 说明

此脚本使用 AndResGuard，对 APK 中的资源文件进行混淆。目的是为了将程序变得更小，同时可以增加别人逆向我们程序的难度。

# 使用步骤

此脚本已在 gradle 中配置，打 release 包时会被自动调用

- 使用 Android Studio 的 Build > Generate Signed APK 正常打包
  只需注意，最后一步的 APK Destination Folder 要选择工程根目录（如 /Users/baoliangyin/app_crystallauncher_android），以便脚本运行时能找到待处理的 APK 包

- 打包完成后：会在工程根目录下 apk/ 目录生成输出文件

	- airlauncher-release.apk：做好资源混淆的 APK 包
	- airlauncher-release-unshrinked：未进行资源混淆的 APK 包
	- airlauncher-release-res-mapping.txt：资源混淆 mapping 文件
	- airlauncher-release-code-mapping.txt：代码混淆 mapping 文件

	两个混淆 mapping 文件，需要拷到 Spark 上

- 对于渠道包，这几个文件名则分别是

	- airlauncher-sp-release.apk：做好资源混淆的 APK 包
	- airlauncher-sp-release-unshrinked：未进行资源混淆的 APK 包
	- airlauncher-sp-release-res-mapping.txt：资源混淆 mapping 文件
	- airlauncher-sp-release-code-mapping.txt：代码混淆 mapping 文件

**config.xml 中的配置需要各位 DEV 仔细添加**

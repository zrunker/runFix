# runFix

热修复，支持Class类（dex文件），SO（so库文件夹），资源文件（apk文件）修复。



## 需求背景：

随着用户量的上升，对一个APP的稳定性要求也越来越高，而在代码的世界中即便是万分小心，还是会出现各种意料之外的异常。对于异常的发生如何快速定位和解决是对程序员的重大考验。

但若每次一发生异常或修改一些简单的代码，就去更新一次APP，这不仅仅增加了程序员自身的工作量，最关键的是十分影响用户体验，那么如何在用户无感知的情况下修复已知异常呢？runFix热修复便是一个很好的解决方案。



## 引入runFix插件：

1. 在根gradle文件中引入jitpack仓库：

```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. 在工程gradle文件中引入MaoKePlayer插件

```
dependencies {
    implementation 'com.github.zrunker:runFix:v1.0'
}
```



## 执行修复 - RunFix：

RunFix类提供了本地和网络两种修复方式，相关API如下：

1. 修复网络文件：

   ```
   // url待修复资源的网络链接
   RunFix.getInstance(context).fixUrl(String url);
   
   // urls待修复资源的网络链接集合
   RunFix.getInstance(context).fixUrlList(ArrayList<String> urls);
   ```

   注意：默认下载url是通过get的方式进行下载。

2. 修复本地文件：

   ```
   // filePath待修复资源文件的本地地址
   // class -> dex文件
   // so -> armeabi，armeabi-v7a，x86，mips，arm64- v8a，mips64，x86_64文件夹
   // res -> apk文件
   RunFix.getInstance(context).fixFile(String filePath);
   ```

3. 修复本地文件夹：

   ```
   // dirPath待修复资源的本地文件夹地址
   RunFix.getInstance(context).fixDir(String dirPath);
   ```

4. 修复默认文件夹（推荐）：

   RunFix的默认修复文件夹地址为${applicationid}/fix目录，所以把待修复资源文件放入该目录下，执行如下方法就能够完成修复功能：

   ```
   // 执行待修复资源默认目录
   RunFix.getInstance(context).fix();
   ```

   补充：

   ```
   // 获取待修复资源默认目录地址
   RunFix.getInstance(context).fixDefaultDir();
   ```



## 待修复文件生成：

runFix支持Class类，SO库以及资源文件的修复，但三种类型文件修复需要包装成特定格式文件才行。

- Class类的修复：需要将待修复的Class类文件一起打包成dex文件。
- SO库的修复：需要将待修复的so库放入特定的armeabi，armeabi-v7a，x86，mips，arm64- v8a，mips64，x86_64文件夹下。
- 资源文件的修复：需要将APP中所有资源文件一起打包成apk文件。



假如将所有待修复文件放入云端提供修复下载，那么该如打包出不同类型的修复文件呢？

### 一. 修复Class类文件【DEX文件】：

**如何打DEX文件包？**

如果工程遇到属于类文件代码导致的BUG，那么就需要修复类文件，并将修复之后的类文件打包成DEX文件，上传至之前设置好的云平台，那么如何打包成DEX文件呢？

![image](https://user-images.githubusercontent.com/27541306/163747829-e6adc2fe-250b-466f-98e9-69ca44b4e2d5.png)

**1、Java文件编译成Class文件，可以使用AS 执行 Make Project，或者使用javac命令执行Java文件。**

1. 编译前：
   ![image](https://user-images.githubusercontent.com/27541306/163748020-9536846c-d35f-42f2-9fac-a29ce04d8c4f.png)

2. 编译后：生成build文件目录，而class文件便在该build文件目录内。
   ![image](https://user-images.githubusercontent.com/27541306/163748060-1faca24d-e42b-4252-836c-8acf2c7bd8a2.png)

找到编辑后的class文件，build/intermediates/javac/debug/classes/....不同AS版本可能目录不一致。

将编辑之后的class文件，连同目录一起复制到一个单独的目录下。

例如：修复的文件是在com.baidu.runFix.MainActivity，那么就将com文件目录拷贝到一个单独目录用于打包DEX文件。
![image](https://user-images.githubusercontent.com/27541306/163748102-49896957-aab6-44d8-8252-46405279e08d.png)

例如将其放置桌面fix目录下：

![image](https://user-images.githubusercontent.com/27541306/163748136-30a05007-aec7-4940-9cb8-a49b5403489c.png)

**2、使用dx工具将class文件打包成dex文件。**

![image](https://user-images.githubusercontent.com/27541306/163748192-8e61c7a9-f032-4def-806e-97ad8409a785.png)

找到Android SDK目录，该目录中将会有dx工具，一般位置是/Users/zoufengli01/Library/Android/sdk/build-tools/28.0.3/dx。

使用shell终端，执行dx命令将会生成DEX文件：

```
/Users/zoufengli01/Library/Android/sdk/build-tools/28.0.3/dx --dex --output=/Users/zoufengli01/Desktop/fix/classes-fix-1.dex /Users/zoufengli01/Desktop/fix
<!--dx --dex --output-->
```

![image](https://user-images.githubusercontent.com/27541306/163748268-2c22d2fc-c5aa-423b-afa4-25e51fb616bc.png)

classes-fix-1.dex就是打包之后的DEX文件，也是需要上传至云端的dex补丁。



## 二、修复资源文件【APK文件】：

只保留APP工程中的资源文件（如：res目录中文件），将其他文件全部删除，然后将剩下文件打包成APK文件，上传至云端提供下载。



## 三、修复so库文件【ZIP文件或文件夹】：

**如何打包SO库？**

将JNI代码编译成So库，一般情况下都是使用NDK进行编译，通常是直接使用NDK执行脚本命令，例如：音视频播放器【NA】一文中是使用Shell命令进行编译。

另外如果是自己开发打包成SO库的话，采用Android Studio+CMake也是十分方便，只需要简单配置CMakeLists.txt：

```
cmake_minimum_required(VERSION 3.10.2)

project("test")

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             native-lib.cpp )

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
```

目录结构如下：

![image](https://user-images.githubusercontent.com/27541306/163748396-413ea034-6359-490c-b104-e93024d42d3a.png)

其中native-lib.cpp便是需要编译的c++文件。

编译好代码之后，保存，点击编译按钮，便会自动生成相应CPU架构SO库：

![image](https://user-images.githubusercontent.com/27541306/163748424-af41c343-5133-4e47-89cc-8f37053690a8.png)

之后将这些编译之后的so库打包上传到云端即可，注意：打包时要将整个CPU架构文件夹都一起打包，而不能只打包单一的so库。

![image](https://user-images.githubusercontent.com/27541306/163748448-fb9f5d8e-c146-4314-a71e-6b33b728fe9e.png)

例如：将libs文件夹打包（将其压缩成zip包）上传至云端提供下载。

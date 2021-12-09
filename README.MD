# 通用WebView

- 原生与JS通讯部分，参考 https://github.com/wendux/DSBridge-Android
- 将WebView的可设置属性都开放出来，项目各自定制。
- 出于实际项目考虑，去除对Android5版本以下的支持
- 自定义错误界面（内置）
- 支持加载进度条颜色设置（不显示，可设置为透明）
- 为了兼容Android5.0以下的input标签，文件选择只支持单选
- 支持自动注入JSBridge
- 可通过继承WebFragment的方式，完成各种自定义

## 引用

1.项目build.gradle文件中添加

```gradle
    implementation 'com.github.Jun1orChan:DWebView-Android:1.2.0'
    //如果使用注解自动注入JSBridge，各模块均需添加
    implementation "com.github.Jun1orChan:dwebview-annotation:1.0.0"
    annotationProcessor "com.github.Jun1orChan:dwebview-compiler:1.0.0"
    implementation 'com.github.getActivity:XXPermissions:13.2'
```

## 使用方法

1. 参考demo中的使用方式，有进行演示如果自定义等操作；如：BounceWebActivity等。
2. 极简添加JSBridge：
```
@JsApi(nameSpace = "echo")
public class JsEchoApi {

    @JavascriptInterface
    public Object syn1(Object a) throws JSONException {
        Log.e("TAG", "syn1=====>args:NULL" + a);
        return new JSONObject();
    }

    @JavascriptInterface
    public Object syn(Object args) throws JSONException {
        Log.e("TAG", "args:" + args);
        return args;
    }

    @JavascriptInterface
    public void asyn(Object args, CompletionHandler handler) {
        handler.complete(args);
    }
}
```
使用注解：@JsApi即可。


3. 如果使用了注解方式注入，需在各模块android.defaultConfig中添加：

```
        javaCompileOptions {
            annotationProcessorOptions {
                arguments = [DWEBVIEW_MODULE_NAME: project.getName()]
            }
        }
```

4.如果要使用JS和原生进行通讯，web开发者需要自行引入dsbridge.js到自己的web界面中。
 ```html
    <script src="./dsbridge.js"> </script>
  ```

5.设置当界面onPause()之后，全局设置web页面内JS可以正常执行的（默认不允许）。

  ```java
     WebFragment.setAllowOnPauseExecuteJs(true);
   ```

6.由于provider标签可以配置文件路径，所以由使用方配置。

 ```xml
        <provider
                android:name="androidx.core.content.FileProvider"
                android:authorities="${applicationId}.fileprovider"
                android:exported="false"
                android:grantUriPermissions="true">
                <meta-data
                    android:name="android.support.FILE_PROVIDER_PATHS"
                    android:resource="@xml/filepaths"/>
        </provider>
  ```

## 说明

1、accept表示获取多媒体文件的类型，有：image/* 、video/* 、audio/*
2、capture表示可以捕获系统默认的设备。比如：camera--照相机；camcorder--摄像机；microphone--录音。

- 图片选择或者拍照（为了保证低版本的兼容性，只允许单选）

```html
<div>图片选择</div>
<input type="file" accept="image/*">
```
- 拍照

```html
<div>拍照</div>
<input type="file" accept="image/*" capture="camera">
```

- 视频选择或者视频录制（为了保证低版本的兼容性，只允许单选）
```html
<div>视频选择</div>
<input type="file" accept="video/*">
```

- 视频录制
```html
<div>视频录制</div>
<input type="file" accept="video/*" capture="camcorder">
```

- 音频选择或者音频录制（iOS暂不支持）
```html
<div>音频选择</div>
<input type="file" accept="audio/*">
```

- 录音（iOS暂不支持）
```html
<div>录音</div>
<input type="file" accept="audio/*" capture="microphone">
```
- 组合（根据accept属性让用户选择拍照、视频录制、音频录制，如果不加capture，则选择项会多出手机相册选项）
```html
<div>录音</div>
<input type="file" accept="image/*,video/*,audio/*" capture="camcorder">
```


## 版本记录

### 2.1.0
- 适配Android 12
- 从相册选择增加“image/*”过滤
- 依赖库改为编译时依赖

### 2.0.0
- 优化库，提升扩展性。
- 修改内置的自定的错误页。
- 支持APT完成自动JSBridge注入。

### 1.1.1
- 更新依赖库
- 最小支持系统改为21

### 1.1.0

- 支持国际化（中文简体-默认、中文繁体-香港、中文繁体-台湾、英文）

### 1.0.3
- 新增申请定位权限前，进行弹框解释说明（合规要求）

### 1.0.0
- 初始提交


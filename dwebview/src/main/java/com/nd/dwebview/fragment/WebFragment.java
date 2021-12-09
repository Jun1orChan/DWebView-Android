package com.nd.dwebview.fragment;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.DOWNLOAD_SERVICE;

import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.SslErrorHandler;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.nd.dialog.ListDialog;
import com.nd.dialog.MaterialDialog;
import com.nd.dialog.listener.OnItemClickListener;
import com.nd.dwebview.R;
import com.nd.dwebview.callback.JavascriptCloseWindowListener;
import com.nd.dwebview.callback.OnWebViewListener;
import com.nd.dwebview.callback.OpenFileChooserCallback;
import com.nd.dwebview.utils.WebViewUtil;
import com.nd.dwebview.wrapper.WebViewWrapperLayout;
import com.nd.util.AppUtil;
import com.nd.util.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 承载WebView
 *
 * @author Administrator
 */
public class WebFragment extends Fragment implements JavascriptCloseWindowListener, OpenFileChooserCallback, DownloadListener {

    /**
     * 默认不允许
     */
    private static boolean sAllowOnPauseExecuteJs = false;
    public static final int REQUEST_CODE_ALL = 0x2001;
    public static final int REQUEST_CODE_TAKEAUDIO = 0x3001;
    public static final int REQUEST_CODE_TAKEPHOTO = 0x4001;
    public static final int REQUEST_CODE_TAKEVIDEO = 0x5001;

    public static final String URL = "url";
    public static final String PROGRESSBAR_COLOR = "progressbar_color";

    private final String MIMETYPE_IMAGE = "image/";
    private final String MIMETYPE_VIDEO = "video/";
    private final String MIMETYPE_AUDIO = "audio/";
    private static String CAPTURE_IMAGE = "";
    private static String CAPTURE_VIDEO = "";
    private static String CAPTURE_AUDIO = "";
    private static String FROM_ALBUM = "";

    private ValueCallback<Uri> mFilePathCallback;
    private ValueCallback<Uri[]> mFilePathCallbackForAndroid5;

    protected WebViewWrapperLayout mWebViewWrapperLayout;
    private Context mContext;

    private File mTakePhotoFile = null;
    private ListDialog mTypeChoiceDialog;
    private volatile boolean mIsChoice;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    /**
     * 是否发生加载错误
     * 通过此标志避免WebView国际化的问题
     */
    private volatile boolean mIsOpenLoadUrlError = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        CAPTURE_IMAGE = mContext.getResources().getString(R.string.dwebview_capture_image);
        CAPTURE_VIDEO = mContext.getResources().getString(R.string.dwebview_capture_video);
        CAPTURE_AUDIO = mContext.getResources().getString(R.string.dwebview_capture_audio);
        FROM_ALBUM = mContext.getResources().getString(R.string.dwebview_from_album);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.dwebview_fragment_web, null, false);
        initViews(viewGroup);
        return viewGroup;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 添加JSApi
        if (Warehouse.JSAPI_ATLAS.size() > 0) {
            for (Map.Entry<String, Class> entry : Warehouse.JSAPI_ATLAS.entrySet()) {
                Object jsObj = null;
                try {
                    jsObj = entry.getValue().newInstance();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (jsObj != null) {
                    mWebViewWrapperLayout.getWebView().addJavascriptObject(jsObj, entry.getKey());
                }
            }
        }
    }

    protected void initViews(ViewGroup viewGroup) {
        mWebViewWrapperLayout = getWebViewWrapper();
        if (mWebViewWrapperLayout == null) {
            mWebViewWrapperLayout = new WebViewWrapperLayout(mContext);
            WebViewUtil.generalSetting(mWebViewWrapperLayout.getWebView());
        }
        if (!TextUtils.isEmpty(getArguments().getString(URL))) {
            mWebViewWrapperLayout.getWebView().loadUrl(getArguments().getString(URL));
        }
        viewGroup.addView(mWebViewWrapperLayout);
        mWebViewWrapperLayout.getWebView().setOpenFileChooserCallback(this);
        mWebViewWrapperLayout.getWebView().setJavascriptCloseWindowListener(this);
        mWebViewWrapperLayout.getWebView().setWebChromeClient(getWebChromeClient());
        mWebViewWrapperLayout.getWebView().setWebViewClient(getWebViewClient());
        mWebViewWrapperLayout.getWebView().setDownloadListener(this);
        if (getArguments().getInt(PROGRESSBAR_COLOR, -1) != -1) {
            mWebViewWrapperLayout.getWebHorizontalProgressBar().setColor(getArguments().getInt(PROGRESSBAR_COLOR));
        }
    }

    /**
     * 获取当前webview包装类，子类可复写此方式，实现重载
     *
     * @return
     */
    public WebViewWrapperLayout getWebViewWrapper() {
        return null;
    }


    public WebChromeClient getWebChromeClient() {
        return new DefaultWebChromeClient();
    }


    public WebViewClient getWebViewClient() {
        return new DefaultWebViewClient();
    }


    public class DefaultWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (URLUtil.isNetworkUrl(url)) {
                return false;
            }
            try {
                // Otherwise allow the OS to handle things like tel, mailto, etc.
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return shouldOverrideUrlLoading(view, request.getUrl().toString());
        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            //提示用户
            final MaterialDialog sslErrorTipsDialog = new MaterialDialog();
            sslErrorTipsDialog
                    .title(view.getContext().getString(R.string.dwebview_tips))
                    .content(view.getContext().getString(R.string.dwebview_ssl_error_tips))
                    .btnText(view.getContext().getString(R.string.dwebview_cancel), view.getContext().getString(R.string.dwebview_ok))
                    .btnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sslErrorTipsDialog.dismiss();
                            handler.cancel();
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            sslErrorTipsDialog.dismiss();
                            //信任所有证书
                            handler.proceed();
                        }
                    })
                    .show(getChildFragmentManager());
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            mIsOpenLoadUrlError = false;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mIsOpenLoadUrlError = true;
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
            mIsOpenLoadUrlError = true;
        }
    }

    public class DefaultWebChromeClient extends WebChromeClient {
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            showTipsDialog(message);
            result.confirm();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result) {
            showConfirmDialog(message, result);
            return true;
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
            //先获取Manifest权限清单列表，判断是否存在定位的权限申请，否则会抛出异常
            List<String> permissionList = getManifestPermissions(getContext().getApplicationContext());
            if (permissionList == null || permissionList.size() == 0) {
                //直接拒绝
                callback.invoke(origin, false, true);
                return;
            }
            List<String> locatePermissionList = new ArrayList<>();
            if (permissionList.contains(Permission.ACCESS_FINE_LOCATION)) {
                locatePermissionList.add(Permission.ACCESS_FINE_LOCATION);
            }
            if (permissionList.contains(Permission.ACCESS_COARSE_LOCATION)) {
                locatePermissionList.add(Permission.ACCESS_COARSE_LOCATION);
            }
            if (locatePermissionList.size() == 0) {
                //直接拒绝
                callback.invoke(origin, false, true);
                return;
            }
            showPermissionExplainDialog(locatePermissionList, origin, callback);
//            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }

        private void showPermissionExplainDialog(final List<String> locatePermissionList, final String origin, final GeolocationPermissions.Callback callback) {
            if (getContext() == null) {
                callback.invoke(origin, false, true);
                return;
            }
            final MaterialDialog explainDialog = new MaterialDialog();
            explainDialog.setCancelable(false);
            explainDialog.setCanceledOnTouchOutside(false);
            explainDialog
                    .title(mContext.getString(R.string.dwebview_tips))
                    .content(mContext.getString(R.string.dwebview_permission_explain_location))
                    .btnText(mContext.getString(R.string.dwebview_cancel), mContext.getString(R.string.dwebview_apply_for))
                    .btnColor(ContextCompat.getColor(getContext(), R.color.dialoglib_gray), ContextCompat.getColor(getContext(), R.color.dialoglib_content))
                    .btnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            explainDialog.dismiss();
                            callback.invoke(origin, false, true);
                        }
                    }, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            explainDialog.dismiss();
                            getPermission(locatePermissionList, origin, callback);
                        }
                    })
                    .show(getChildFragmentManager());
        }

        private void getPermission(List<String> locatePermissionList, final String origin, final GeolocationPermissions.Callback callback) {
            XXPermissions.with(WebFragment.this)
                    .permission(locatePermissionList)
                    .request(new OnPermissionCallback() {
                        @Override
                        public void onGranted(List<String> permissions, boolean all) {
                            callback.invoke(origin, true, true);
                        }

                        @Override
                        public void onDenied(List<String> permissions, boolean never) {
                            callback.invoke(origin, false, true);
                            showPermissionDeniedDialog(String.format(mContext.getString(R.string.dwebview_location_permission_denied_tips),
                                    AppUtil.getAppName(mContext.getApplicationContext()), AppUtil.getAppName(mContext.getApplicationContext())));
                        }
                    });
        }

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            //发生加载错误，不设置标题
            if (mIsOpenLoadUrlError) {
                return;
            }
            if (getActivity() != null && getActivity() instanceof OnWebViewListener) {
                ((OnWebViewListener) getActivity()).onReceiveTitle(title);
            }
        }
    }

    private void showTipsDialog(String msg) {
        if (getActivity() == null) {
            return;
        }
        MaterialDialog tipsDialog = new MaterialDialog();
        tipsDialog
                .content(msg)
                .btnText(getString(R.string.dwebview_ok))
                .show(getChildFragmentManager());
    }

    private void showConfirmDialog(String message, final JsResult result) {
        if (getActivity() == null) {
            return;
        }
        final MaterialDialog confirmDialog = new MaterialDialog();
        confirmDialog
                .content(message)
                .btnText(getString(R.string.dwebview_cancel), getString(R.string.dwebview_ok))
                .btnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDialog.dismiss();
                        result.cancel();
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        confirmDialog.dismiss();
                        result.confirm();
                    }
                })
                .show(getChildFragmentManager());
    }

    public boolean canGoBack() {
        if (mWebViewWrapperLayout == null) {
            return false;
        } else {
            return mWebViewWrapperLayout.getWebView().canGoBack();
        }
    }

    public void goBack() {
        if (mWebViewWrapperLayout != null) {
            mWebViewWrapperLayout.getWebView().goBack();
        }
    }


    @Override
    public boolean onClose() {
        if (getActivity() != null) {
            getActivity().finish();
        }
        return false;
    }

//    @Override
//    public void openFileChooserCallBack(ValueCallback<Uri> filePathCallback, String acceptType, String capture) {
//        mFilePathCallback = filePathCallback;
//        acceptType = acceptType.replace(",", ";");
//        actionByAcceptType(acceptType, !TextUtils.isEmpty(capture));
//    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void openFileChooserCallBack(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        mFilePathCallbackForAndroid5 = filePathCallback;
        String acceptType = "";
        if (fileChooserParams.getAcceptTypes() != null && fileChooserParams.getAcceptTypes().length > 0) {
            for (int i = 0; i < fileChooserParams.getAcceptTypes().length; i++) {
                if (TextUtils.isEmpty(fileChooserParams.getAcceptTypes()[i])) {
                    continue;
                }
                if (!TextUtils.isEmpty(acceptType)) {
                    acceptType += ";" + fileChooserParams.getAcceptTypes()[i];
                } else {
                    acceptType = fileChooserParams.getAcceptTypes()[i];
                }
            }
        }
        actionByAcceptType(acceptType, fileChooserParams.isCaptureEnabled());
    }

    /**
     * 为了保持5.0以下的文件选择，统一只能进行单选
     *
     * @param acceptType
     */
    private void actionByAcceptType(String acceptType, boolean captureEnabled) {
        if (TextUtils.isEmpty(acceptType)) {
            acceptType = "*/*";
        }
        List<String> itemList = new ArrayList<>();
        if (acceptType.contains("*/*")) {
            itemList.add(CAPTURE_IMAGE);
            itemList.add(CAPTURE_VIDEO);
            itemList.add(CAPTURE_AUDIO);
        }
        if (acceptType.contains(MIMETYPE_IMAGE)) {
            if (!itemList.contains(CAPTURE_IMAGE)) {
                itemList.add(CAPTURE_IMAGE);
            }
        }
        if (acceptType.contains(MIMETYPE_VIDEO)) {
            if (!itemList.contains(CAPTURE_VIDEO)) {
                itemList.add(CAPTURE_VIDEO);
            }
        }
        if (acceptType.contains(MIMETYPE_AUDIO)) {
            if (!itemList.contains(CAPTURE_AUDIO)) {
                itemList.add(CAPTURE_AUDIO);
            }
        }
        if (itemList.size() == 0) {
            startFileChooser(acceptType, REQUEST_CODE_ALL);
            return;
        }
        if (captureEnabled) {
            captureAction(itemList);
        } else {
            choiceAction(itemList, acceptType);
        }
    }

    /**
     * 拍照等行为解析
     *
     * @param itemList
     */
    private void captureAction(final List<String> itemList) {
        if (itemList.size() == 1) {
            //直接启动对应的录制界面
            startCapture(itemList.get(0));
        } else {
            //多种类型
            final OnItemClickListener onItemClickListener = new OnItemClickListener() {
                @Override
                public void onItemClick(int i) {
                    mIsChoice = true;
                    startCapture(itemList.get(i));
                    if (mTypeChoiceDialog != null) {
                        mTypeChoiceDialog.dismiss();
                    }
                }
            };
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    showTypeChoiceDialog(itemList.toArray(new CharSequence[itemList.size()]), onItemClickListener);
                }
            });
        }
    }

    /**
     * 行为权限申请
     *
     * @param captureName
     */
    private void startCapture(String captureName) {
        if (captureName.contains(CAPTURE_IMAGE)) {
            requestPermission(getTakePhotoRunnable(), String.format(mContext.getString(R.string.dwebview_camera_permission_denied_tips),
                    AppUtil.getAppName(mContext.getApplicationContext()),
                    AppUtil.getAppName(mContext.getApplicationContext())),
                    Permission.CAMERA);
        } else if (captureName.contains(CAPTURE_VIDEO)) {
            requestPermission(getTakeVideoRunnable(), String.format(mContext.getString(R.string.dwebview_video_permission_denied_tips),
                    AppUtil.getAppName(mContext.getApplicationContext()), AppUtil.getAppName(mContext.getApplicationContext())),
                    Permission.CAMERA);
        } else if (captureName.contains(CAPTURE_AUDIO)) {
            requestPermission(getTakeAudioRunnable(), String.format(mContext.getString(R.string.dwebview_audio_permission_denied_tips),
                    AppUtil.getAppName(mContext.getApplicationContext()),
                    AppUtil.getAppName(mContext.getApplicationContext())),
                    Permission.RECORD_AUDIO);
        }
    }

    /**
     * 显示 行为类型选择dialog
     *
     * @param itemArray           名称列表
     * @param onItemClickListener 回调
     */
    private void showTypeChoiceDialog(final CharSequence[] itemArray, OnItemClickListener onItemClickListener) {
        if (mTypeChoiceDialog == null) {
            mTypeChoiceDialog = new ListDialog();
        }
        mTypeChoiceDialog.itemArray(itemArray)
                .itemClickListener(onItemClickListener)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        if (!mIsChoice) {
                            sendFile2Web(null);
                        }
                        mIsChoice = false;
                    }
                });
        mTypeChoiceDialog.show(getChildFragmentManager());
    }

    /**
     * 行为构造
     *
     * @param itemList
     * @param acceptType
     */
    private void choiceAction(final List<String> itemList, final String acceptType) {
        itemList.add(FROM_ALBUM);
        final OnItemClickListener onItemClickListener = new OnItemClickListener() {
            @Override
            public void onItemClick(int i) {
                mIsChoice = true;
                if (itemList.get(i).equals(FROM_ALBUM)) {
                    //单击了从相册选择
                    startFileChooser("image/*", REQUEST_CODE_ALL);
                } else {
                    startCapture(itemList.get(i));
                }
                if (mTypeChoiceDialog != null) {
                    mTypeChoiceDialog.dismiss();
                }
            }
        };
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                showTypeChoiceDialog(itemList.toArray(new CharSequence[itemList.size()]), onItemClickListener);
            }
        });
    }

    private Runnable getTakePhotoRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                takePhoto();
            }
        };
    }

    /**
     * 拍照
     */
    private void takePhoto() {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        captureIntent.addCategory(Intent.CATEGORY_DEFAULT);
        File photoFile = new File(StorageUtil.getCacheDirectory(getActivity()).getAbsolutePath() + File.separator + "photos");
        if (!photoFile.exists()) {
            photoFile.mkdirs();
        }
        mTakePhotoFile = new File(photoFile, "IMG" + System.currentTimeMillis() + ".png");
        Uri takePhotoUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            takePhotoUri = FileProvider.getUriForFile(getActivity(), getActivity().getPackageName() + ".fileprovider", mTakePhotoFile);
            captureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            takePhotoUri = Uri.fromFile(mTakePhotoFile);
        }
        captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, takePhotoUri);
        try {
            startActivityForResult(captureIntent, REQUEST_CODE_TAKEPHOTO);
        } catch (Exception e) {
            e.printStackTrace();
            if (getActivity() != null) {
                Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.dwebview_capture_image_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 适配录制
     *
     * @return
     */
    private Runnable getTakeVideoRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                takeVideo();
            }
        };
    }

    /**
     * 视频录制
     */
    private void takeVideo() {
        Intent intent = new Intent();
        try {
            intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CODE_TAKEVIDEO);
        } catch (Exception e) {
            sendFile2Web(null);
            e.printStackTrace();
            if (getActivity() != null) {
                Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.dwebview_capture_video_failed), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 录音
     */
    private Runnable getTakeAudioRunnable() {
        return new Runnable() {
            @Override
            public void run() {
                takeAudio();
            }
        };
    }

    /**
     * 录音
     */
    private void takeAudio() {
        try {
            Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivityForResult(intent, REQUEST_CODE_TAKEAUDIO);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.dwebview_capture_audio_failed), Toast.LENGTH_SHORT).show();
            sendFile2Web(null);
        }
    }

    /**
     * 权限申请
     *
     * @param runnable
     * @param deniedTip
     * @param permissions
     */
    private void requestPermission(final Runnable runnable, final String deniedTip, @NonNull String... permissions) {
        XXPermissions.with(this)
                .permission(permissions)
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        runnable.run();
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        sendFile2Web(null);
                        showPermissionDeniedDialog(deniedTip);
                    }
                });
    }

    /**
     * 文件选择
     *
     * @param acceptType
     * @param requestCode
     */
    private void startFileChooser(String acceptType, int requestCode) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (!TextUtils.isEmpty(acceptType)) {
            i.setType(acceptType);
        }
        try {
            startActivityForResult(Intent.createChooser(i, mContext.getString(R.string.dwebview_file_choose)), requestCode);
        } catch (Exception e) {
            e.printStackTrace();
            if (getActivity() != null) {
                Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.dwebview_file_choose_failed), Toast.LENGTH_SHORT).show();
            }
            sendFile2Web(null);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            sendFile2Web(null);
            return;
        }
        if (requestCode == REQUEST_CODE_ALL || requestCode == REQUEST_CODE_TAKEAUDIO
                || requestCode == REQUEST_CODE_TAKEVIDEO) {
            if (data == null) {
                Toast.makeText(mContext.getApplicationContext(), mContext.getString(R.string.dwebview_get_media_file_failed), Toast.LENGTH_SHORT).show();
                sendFile2Web(null);
                return;
            }
            Uri uri = data.getData();
            sendFile2Web(uri);
        } else if (requestCode == REQUEST_CODE_TAKEPHOTO) {
            sendFile2Web(Uri.fromFile(mTakePhotoFile));
        } else if (requestCode == REQUEST_CODE_TAKEVIDEO) {
            Uri videoUri = data.getData();
            sendFile2Web(videoUri);
        }
    }

    /**
     * 发送文件给web
     *
     * @param fileUri
     */
    private void sendFile2Web(Uri fileUri) {
        if (mFilePathCallback != null) {
            mFilePathCallback.onReceiveValue(fileUri);
            mFilePathCallback = null;
        } else if (mFilePathCallbackForAndroid5 != null) {
            try {
                if (fileUri != null) {
                    mFilePathCallbackForAndroid5.onReceiveValue(new Uri[]{fileUri});
                } else {
                    mFilePathCallbackForAndroid5.onReceiveValue(null);
                }
                mFilePathCallbackForAndroid5 = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDownloadStart(final String url, String userAgent, final String contentDisposition, final String mimetype, long contentLength) {
        if (getActivity() == null) {
            return;
        }
        //进行下载提示
        final MaterialDialog downloadConfirmDialog = new MaterialDialog();
        downloadConfirmDialog
                .title(mContext.getString(R.string.dwebview_tips))
                .content(mContext.getString(R.string.dwebview_dialog_download_content) + "\n" + URLUtil.guessFileName(url, contentDisposition, mimetype))
                .btnText(mContext.getString(R.string.dwebview_cancel), mContext.getString(R.string.dwebview_download))
                .btnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadConfirmDialog.dismiss();
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        downloadConfirmDialog.dismiss();
                        downloadBySystem(url, contentDisposition, mimetype);
                    }
                }).show(getChildFragmentManager());
    }

    /**
     * 下载
     *
     * @param url
     * @param contentDisposition
     * @param mimeType
     */
    private void downloadBySystem(String url, String contentDisposition, String mimeType) {
        // 指定下载地址
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
        request.allowScanningByMediaScanner();
        // 设置通知的显示类型，下载进行时和完成后显示通知
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // 设置通知栏的标题，如果不设置，默认使用文件名
//        request.setTitle("This is title");
        // 设置通知栏的描述
//        request.setDescription("This is description");
        // 允许在计费流量下下载
        request.setAllowedOverMetered(true);
        // 允许该记录在下载管理界面可见
        request.setVisibleInDownloadsUi(true);
        // 允许漫游时下载
        request.setAllowedOverRoaming(true);
        // 允许下载的网路类型
//        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE);
        // 设置下载文件保存的路径和文件名
        String fileName = URLUtil.guessFileName(url, contentDisposition, mimeType);
        /**
         * 目录: Android -> data -> xxx.xxx.xxx -> files -> Download -> dxtj.apk
         * 这个文件是你的应用所专用的,软件卸载后，下载的文件将随着卸载全部被删除
         */
        request.setDestinationInExternalFilesDir(getActivity(), Environment.DIRECTORY_DOWNLOADS, fileName);
        final DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
        // 添加一个下载任务
        long downloadId = downloadManager.enqueue(request);
//        Log.e("downloadId:{}", downloadId + "");
    }

    /**
     * 显示权限拒绝dialog
     *
     * @param tipMsg
     */
    private void showPermissionDeniedDialog(String tipMsg) {
        if (getActivity() == null) {
            return;
        }
        final MaterialDialog audioPermissionDeniedDialog = new MaterialDialog();
        audioPermissionDeniedDialog
                .content(tipMsg)
                .btnText(getString(R.string.dwebview_cancel), getString(R.string.dwebview_btn_text_denied_setting))
                .btnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        audioPermissionDeniedDialog.dismiss();
                        sendFile2Web(null);
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        audioPermissionDeniedDialog.dismiss();
                        AppUtil.goAppDetailsSettings(getActivity());
                    }
                })
                .show(getChildFragmentManager());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWebViewWrapperLayout != null) {
            mWebViewWrapperLayout.getWebView().onResume();
            if (!sAllowOnPauseExecuteJs) {
                mWebViewWrapperLayout.getWebView().resumeTimers();
            }
        }
    }

    @Override
    public void onPause() {
        if (mWebViewWrapperLayout != null) {
            mWebViewWrapperLayout.getWebView().onPause();
            if (!sAllowOnPauseExecuteJs) {
                mWebViewWrapperLayout.getWebView().pauseTimers();
            }
        }
        super.onPause();
    }


    @Override
    public void onDestroyView() {
        if (mWebViewWrapperLayout != null) {
            mWebViewWrapperLayout.removeWebView();
        }
        mMainHandler.removeCallbacksAndMessages(null);
        super.onDestroyView();
    }

    public static void setAllowOnPauseExecuteJs(boolean allowOnPauseExecuteJs) {
        sAllowOnPauseExecuteJs = allowOnPauseExecuteJs;
    }


    /**
     * 返回应用程序在清单文件中注册的权限
     */
    private List<String> getManifestPermissions(Context context) {
        try {
            String[] requestedPermissions = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions;
            // 当清单文件没有注册任何权限的时候，那么这个数组对象就是空的
            // https://github.com/getActivity/XXPermissions/issues/35
            return asArrayList(requestedPermissions);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将数组转换成 ArrayList
     * <p>
     * 这里解释一下为什么不用 Arrays.asList
     * 第一是返回的类型不是 java.util.ArrayList 而是 java.util.Arrays.ArrayList
     * 第二是返回的 ArrayList 对象是只读的，也就是不能添加任何元素，否则会抛异常
     */
    @SuppressWarnings("all")
    private <T> ArrayList<T> asArrayList(T... array) {
        if (array == null || array.length == 0) {
            return null;
        }
        ArrayList<T> list = new ArrayList<>(array.length);
        for (T t : array) {
            list.add(t);
        }
        return list;
    }
}

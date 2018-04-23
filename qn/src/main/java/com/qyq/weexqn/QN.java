package com.qyq.weexqn;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.base.library.FileUtil;
import com.qiniu.android.common.FixedZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.KeyGenerator;
import com.qiniu.android.storage.UpCancellationSignal;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UpProgressHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.android.storage.UploadOptions;
import com.qiniu.android.storage.persistent.FileRecorder;
import com.qiniu.android.utils.AsyncRun;
import com.qiniu.android.utils.UrlSafeBase64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 所有的网络操作均使用独立的线程异步运行，UpCompletionHandler##complete、UpProgressHandler##progress 是在主线程调用的，在回调函数内可以直接操作 UI 控件。
 * Created by QYG_XXY on 0012 2018/3/12.
 */

public final class QN {
    private static final String TAG = "QN";
    private static UploadManager uploadManager = null;
    private static final String RESUMABLE_UPLOAD_WITHOUT_KEY_PATH = "api/im/qiNiuCloud/v1/getUpToken";

    private QN() {
    }

    public static void init(Context context) {
        if (context == null) return;
        if (uploadManager == null) {
            synchronized (QN.class) {
                if (uploadManager == null) {
                    initUploadManager(context);
                }
            }
        }
    }

    private static void initUploadManager(Context context) {
        KeyGenerator keyGenerator = new KeyGenerator() {
            // 指定一个进度文件名，用文件路径和最后修改时间做hash
            // generator
            @Override
            public String gen(String key, File file) {
                String recorderName = System.currentTimeMillis() + ".progress";
                try {
                    recorderName = UrlSafeBase64.encodeToString(Tools.sha1(file.getAbsolutePath() + ":" + file.lastModified())) + ".progress";
                } catch (NoSuchAlgorithmException e) {
                    Log.e("QN", e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    Log.e("QN", e.getMessage());
                }
                return recorderName;
            }
        };

        Configuration.Builder builder = new Configuration.Builder();
        builder.chunkSize(512 * 1024)        // 分片上传时，每片的大小。 默认256K
                .putThreshhold(1024 * 1024)   // 启用分片上传阀值。默认512K
                .connectTimeout(10)           // 链接超时。默认10秒
                .responseTimeout(60)          // 服务器响应超时。默认60秒
                .zone(FixedZone.zone2);       // 设置区域，指定不同区域的上传域名、备用域名、备用IP。
        //.useHttps(true)               // 是否使用https上传域名
        try {
            //.recorder(recorder)           // recorder分片上传时，已上传片记录器。默认null
            builder.recorder(new FileRecorder(context.getFilesDir() + "/QiniuAndroid"), keyGenerator); // keyGen 分片上传时，生成标识符，用于片记录器区分是那个文件的上传记录
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "QN create file error!!! initUploadManager(Context context) ");
        }
        Configuration config = builder.build();
        uploadManager = new UploadManager(config);// 重用uploadManager。一般地，只需要创建一个uploadManager对象
    }

    /**
     * 获取UploadManager
     *
     * @return
     */
    public static UploadManager getUploadManager() {
        if (uploadManager == null)
            throw new RuntimeException("uploadManager == null !!! Call  init(Context context) ");
        return uploadManager;
    }

    /**
     * 上传七牛
     *
     * @param context
     * @param userId
     * @param path
     * @param callback
     */
    public static void Updata(final Context context, final String baseUrl, final String userId, final String path, final UpdataCallback callback) {
        getToken(context, baseUrl, userId, new GetQNTokenCallback() {
            @Override
            public void callback(int code, String msg, QNTokenBean tokenBean) {
                if (code != 0) {
                    if (callback != null)
                        callback.callback(code, msg, null, null, null);
                    return;
                }
                if (tokenBean == null || tokenBean.getData() == null || TextUtils.isEmpty(tokenBean.getData().getToken())) {
                    if (callback != null)
                        callback.callback(-106, "Updata::: token is null !!!", null, null, null);
                    return;
                }
                UploadOptions uploadOptions = new UploadOptions(
                        null,
                        null,
                        false,
                        new UpProgressHandler() {
                            @Override
                            public void progress(String key, double percent) {
                                Log.e("UploadOptions", "progress:::" + percent);
                                if (callback != null) {
                                    callback.progress(key, percent);
                                }
                            }
                        },
                        new UpCancellationSignal() {

                            @Override
                            public boolean isCancelled() {
                                return false;
                            }
                        });
                int random = (int) (Math.random() * 10000);
                String key = tokenBean.getData().getDate() + "/" + new Date().getTime() + "_" + random + "." + FileUtil.getExtensionName(path);
                getUploadManager().put(
                        new File(path),
                        key,
                        tokenBean.getData().getToken(),
                        new UpCompletionHandler() {
                            @Override
                            public void complete(final String key, final ResponseInfo respInfo, final JSONObject jsonData) {
                                if (respInfo.isOK()) {
//                                    try {
//                                    String fileKey = jsonData.getString("key");
//                                    String fileHash = jsonData.getString("hash");
//                                    writeLog("File Size: " + Tools.formatSize(uploadFileLength));
//                                    writeLog("File Key: " + fileKey);
//                                    writeLog("File Hash: " + fileHash);
//                                    writeLog("Last Time: " + Tools.formatMilliSeconds(lastMillis));
//                                    writeLog("Average Speed: " + Tools.formatSpeed(fileLength, lastMillis));
//                                    writeLog("X-Reqid: " + respInfo.reqId);
//                                    writeLog("X-Via: " + respInfo.xvia);
//                                    writeLog("--------------------------------");
                                    AsyncRun.runInMain(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (callback != null)
                                                callback.callback(0, "success", key, respInfo, jsonData);
                                        }
                                    });
//                                    } catch (JSONException e) {
//                                        e.printStackTrace();
//                                        AsyncRun.runInMain(new Runnable() {
//                                            @Override
//                                            public void run() {
//                                                //上传回复解析错误 jsonData==null ?null:jsonData.toString()
//                                            }
//                                        });
//                                    }
                                } else {
                                    AsyncRun.runInMain(new Runnable() {
                                        @Override
                                        public void run() {
                                            //上传文件失败
                                            if (callback != null)
                                                callback.callback(-107, "updata file error!!!", key, respInfo, jsonData);
                                        }
                                    });
                                }
                            }
                        },
                        uploadOptions);
            }
        });
    }

    /**
     * 从业务服务器获取上传凭证
     *
     * @param context
     */
    public static void getToken(final Context context, final String baseUrl, final String userId, final GetQNTokenCallback callback) {
        new Thread(new Runnable() {
            @Override
            public void run() {
//                final QNTokenBean tokenBean = QNTokenBean.getInstall(context);
//                if (tokenBean != null && !TextUtils.isEmpty(tokenBean.getUploadToken()) && userId.equals(tokenBean.getUserId())) {
//                    if (tokenBean.getTime() == -1 || tokenBean.getTime() > new Date().getTime()) {
//                        AsyncRun.runInMain(new Runnable() {
//                            @Override
//                            public void run() {
//                                callback.callback(0, "success", tokenBean);
//                            }
//                        });
//                        return;
//                    }
//                }
                final OkHttpClient httpClient = new OkHttpClient();
                String url = baseUrl + RESUMABLE_UPLOAD_WITHOUT_KEY_PATH;
                Request req = new Request.Builder().url(url).method("GET", null).build();
                Response resp = null;
                try {
                    resp = httpClient.newCall(req).execute();
                    JSONObject jsonObject = new JSONObject(resp.body().string());
                    int code = jsonObject.getInt("code");
                    String message = jsonObject.getString("message");
                    JSONObject data = jsonObject.getJSONObject("data");
                    String date = data.getString("date");
                    String token = data.getString("token");
                    //申请凭证
                    //upload(uploadToken);上传逻辑
                    final QNTokenBean bean = new QNTokenBean();
                    bean.setCode(code);
                    bean.setMessage(message);
                    QNTokenBean.TokenBean tokenBean = new QNTokenBean.TokenBean();
                    tokenBean.setDate(date);
                    tokenBean.setToken(token);
                    bean.setData(tokenBean);
                    AsyncRun.runInMain(new Runnable() {
                        @Override
                        public void run() {
                            callback.callback(0, "success", bean);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    AsyncRun.runInMain(new Runnable() {
                        @Override
                        public void run() {
                            //申请上传凭证失败
                            callback.callback(-105, "getToken error !!!", null);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    AsyncRun.runInMain(new Runnable() {
                        @Override
                        public void run() {
                            //申请上传凭证失败
                            callback.callback(-106, "getToken error !!!", null);
                        }
                    });
                    if (resp != null) {
                        Log.e("getToken", "StatusCode:" + resp.code());
                        Log.e("getToken", "Response:" + resp.toString());
                    }
                    Log.e("getToken", "Exception:" + e.getMessage());
                } finally {
                    if (resp != null) {
                        resp.body().close();
                    }
                }
            }
        }).start();
    }

    public interface GetQNTokenCallback {
        void callback(int code, String msg, QNTokenBean tokenBean);
    }

    public interface UpdataCallback {
        void callback(int code, String msg, String key, ResponseInfo respInfo, JSONObject jsonData);

        /**
         * 注意：progress(key, percent) 中的 key 即 uploadManager.put(file, key, ...) 方法指定的 key。
         *
         * @param key
         * @param percent
         */
        void progress(String key, double percent);
    }
}

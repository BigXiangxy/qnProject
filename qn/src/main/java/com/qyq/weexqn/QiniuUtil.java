//package com.qyq.weexqn;
//
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//
//import com.chewuwuyou.baselibrary.bean.UploadImgFinishBean;
//import com.chewuwuyou.baselibrary.bean.UploadImgParam;
//import com.chewuwuyou.baselibrary.utls.Cache.CacheTools;
//import com.chewuwuyou.baselibrary.utls.Cache.FileUtils;
//import com.qiniu.android.common.AutoZone;
//import com.qiniu.android.http.ResponseInfo;
//import com.qiniu.android.storage.Configuration;
//import com.qiniu.android.storage.UpCompletionHandler;
//import com.qiniu.android.storage.UploadManager;
//
//import org.json.JSONObject;
//import org.lasque.tusdk.core.utils.sqllite.ImageSqlInfo;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import de.greenrobot.event.EventBus;
//import rx.Observable;
//import rx.Subscriber;
//import rx.android.schedulers.AndroidSchedulers;
//import rx.functions.Func1;
//import rx.schedulers.Schedulers;
//
///**
// * 七牛图片处理工具类
// * Created by yuyong on 16/12/14.
// */
//
//public class QiniuUtil {
//    private static final String QI_NIU_BASE_URL = "https://ogroic7v0.qnssl.com/";
//
//    private static List<String> imgLists = new ArrayList<>();//图片集合
//
//    /**
//     * 七牛单张上传图片
//     *
//     * @param context
//     * @param imagePath
//     * @param className
//     */
//    public static void upLoadImg(final Context context, String imagePath, final String className) {
//        final ProgressDialog mProgressDialog = new ProgressDialog(context);
//        mProgressDialog.setMessage("正在上传");
//        mProgressDialog.setCancelable(true);
//        mProgressDialog.show();
//        Configuration config = new Configuration.Builder().zone(new AutoZone( null)).build();
//        UploadManager uploadManager = new UploadManager(config);
//        uploadManager.put(imagePath, String.valueOf(System.currentTimeMillis()), CacheTools.getUserData("qiniutoken"), new UpCompletionHandler() {
//            @Override
//            public void complete(String key, ResponseInfo info, JSONObject res) {
//                if (info.statusCode == 200) {
//                    EventBus.getDefault().post(new UploadImgFinishBean(QI_NIU_BASE_URL + key, className));
//                    mProgressDialog.dismiss();
//                } else {
//                    TokenObtain tokenObtain = new TokenObtain();
//                    tokenObtain.Group(context);
//                    ToastUtil.toastShow(context, "图片上传失败");
//                    mProgressDialog.dismiss();
//                }
//            }
//        }, null);
//    }
//
//    /**
//     * 七牛单张上传图片
//     *
//     * @param context
//     * @param imageBytes
//     * @param className
//     */
//    public static void upLoadImg(final Context context, byte[] imageBytes, final String className) {
//        Configuration config = new Configuration.Builder().zone(new AutoZone( null)).build();
//        UploadManager uploadManager = new UploadManager(config);
//        uploadManager.put(imageBytes, String.valueOf(System.currentTimeMillis()), CacheTools.getUserData("qiniutoken"), new UpCompletionHandler() {
//            @Override
//            public void complete(String key, ResponseInfo info, JSONObject res) {
//                if (info.statusCode == 200) {
//                    EventBus.getDefault().post(new UploadImgFinishBean(QI_NIU_BASE_URL + key, className));
//                } else {
//                    TokenObtain tokenObtain = new TokenObtain();
//                    tokenObtain.Group(context);
//                    ToastUtil.toastShow(context, "图片上传失败");
//                    EventBus.getDefault().post(new UploadImgFinishBean("", className));
//                }
//            }
//        }, null);
//    }
//
//    /**
//     * 七牛多张上传图片
//     *
//     * @param context
//     * @param imgPaths
//     */
//    public static void upLoadImgList(final Context context, final List<String> imgPaths, String className, ProgressDialog mProgressDialog) {
//        if (imgPaths == null || imgPaths.size() <= 0)
//            return;
//        imgLists.clear();
//        for (int i = 0; i < imgPaths.size(); i++) {
//            uploadImg(context, imgPaths.get(i).toString().trim(), mProgressDialog, imgPaths.size(), className, i);
//        }
//    }
//
//    public static void clearList() {
//        if (imgLists != null) imgLists.clear();
//    }
//
//    /**
//     * 异步线程单张上传图片
//     *
//     * @param context
//     * @param imageStr
//     * @param dialog
//     * @param imgSize
//     * @param className
//     */
//    public static void uploadImg(final Context context, final String imageStr, final ProgressDialog dialog, final int imgSize, final String className, int i) {
//        if (context == null)
//            return;
//        UploadImgParam uploadImgParam = new UploadImgParam(i, imageStr);
//        Observable.just(uploadImgParam).map(new Func1<UploadImgParam, String>() {
//            @Override
//            public String call(UploadImgParam uploadImgParam) {
//                return new TokenObtain().qiniuImg(context, FileUtils.compressImage(FileUtils.getSmallBitmap(uploadImgParam.getImgStr())), "cdd" + uploadImgParam.getPosition());
//            }
//        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onNext(String imgStr) {
//                Configuration config = new Configuration.Builder().zone(new AutoZone( null)).build();
//                UploadManager uploadManager = new UploadManager(config);
//                uploadManager.put(imgStr, String.valueOf(System.currentTimeMillis()), CacheTools.getUserData("qiniutoken"), new UpCompletionHandler() {
//                    @Override
//                    public void complete(String key, ResponseInfo info, JSONObject res) {
//                        if (info.statusCode == 200) {
//                            imgLists.add(QI_NIU_BASE_URL + key);
//                            if (imgLists.size() == imgSize) {
//                                EventBus.getDefault().post(new UploadImgFinishBean(imgLists, className));
//                            }
//                        } else {
//                            TokenObtain tokenObtain = new TokenObtain();
//                            tokenObtain.Group(context);
//                            ToastUtil.toastShow(context, "数据载入失败");
//                            if (dialog != null)
//                                dialog.dismiss();
//                        }
//                    }
//                }, null);
//
//            }
//
//        });
//
//        if (dialog != null) {
//            dialog.setCancelable(true);
//            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//                @Override
//                public void onCancel(DialogInterface dialog) {
//                    ToastUtil.toastShow(context, "取消上传图片");
//                }
//            });
//        }
//
//    }
//
//    /**
//     * 七牛多张上传图片
//     *
//     * @param context
//     * @param imageSqlInfos
//     */
//    public static void upLoadImgInfoList(final Context context, final List<ImageSqlInfo> imageSqlInfos, String className, ProgressDialog mProgressDialog) {
//        if (imageSqlInfos == null || imageSqlInfos.size() <= 0)
//            return;
//        imgLists.clear();
//        for (int i = 0; i < imageSqlInfos.size(); i++) {
//            uploadImg(context, imageSqlInfos.get(i).path, mProgressDialog, imageSqlInfos.size(), className, i);
//        }
//    }
//
//    /**
//     * 七牛上传图片
//     *
//     * @param context
//     * @param imagePaths 图片集合
//     * @param callBack
//     */
//    public static void uploadImgList(final Context context, final List<String> imagePaths, final UploadResultCallBack callBack) {
//        if (context == null || imagePaths == null || imagePaths.size() == 0)
//            return;
//        int size = imagePaths.size();
//        if (CacheTools.getUserData("qiniutoken") == null) {//七牛token为空
//            TokenObtain tokenObtain = new TokenObtain();
//            tokenObtain.Group(context);
//        }
//        for (int i = 0; i < size; i++) {
//            upload(context, new UploadImgParam(i, imagePaths.get(i)), size, callBack);
//        }
//    }
//
//    /**
//     * 七牛上传图片
//     *
//     * @param context
//     * @param uploadImgParam
//     * @param imgSize
//     * @param callBack
//     */
//    public static void upload(final Context context, UploadImgParam uploadImgParam, final int imgSize, final UploadResultCallBack callBack) {
//        Observable.just(uploadImgParam).map(new Func1<UploadImgParam, String>() {
//            @Override
//            public String call(UploadImgParam uploadImgParam) {
//                return new TokenObtain().qiniuImg(context, FileUtils.compressImage(FileUtils.getSmallBitmap(uploadImgParam.getImgStr())), "cdd" + uploadImgParam.getPosition());
//            }
//        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<String>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                e.printStackTrace();
//            }
//
//            @Override
//            public void onNext(String imgStr) {
//                Configuration config = new Configuration.Builder().zone(new AutoZone(null)).build();
//                UploadManager uploadManager = new UploadManager(config);
//                uploadManager.put(imgStr, String.valueOf(System.currentTimeMillis()), CacheTools.getUserData("qiniutoken"), new UpCompletionHandler() {
//                    @Override
//                    public void complete(String key, ResponseInfo info, JSONObject res) {
//                        if (info.statusCode == 200) {
//                            imgLists.add(QI_NIU_BASE_URL + key);
//                            if (imgLists.size() == imgSize) {
//                                callBack.success(imgLists);
//                            }
//                        } else {
//                            callBack.failure(info);
//                        }
//                    }
//                }, null);
//
//            }
//
//        });
//
//    }
//
//    public interface UploadResultCallBack {
//        void success(List<String> path);
//
//        void failure(ResponseInfo info);
//    }
//
//}

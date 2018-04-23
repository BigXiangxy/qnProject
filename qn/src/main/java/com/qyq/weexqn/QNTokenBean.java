package com.qyq.weexqn;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.base.library.LastingUtils;

import java.io.Serializable;

/**
 * 七牛
 * Created by xxy on 2016/7/21.
 */
public class QNTokenBean implements Serializable {

    private int code;
    private String message;
    private TokenBean data;


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public TokenBean getData() {
        return data;
    }

    public void setData(TokenBean data) {
        this.data = data;
    }

    public static class TokenBean {
        private String date;
        private String token;

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }


    private static final String qn_token_key = "QN_TOKEN_KEY";
    private static QNTokenBean _install;

    /**
     * 获取实例,如果有缓存系统将获取缓存中的对象
     *
     * @return
     */
    public static QNTokenBean getInstall(Context context) {
        if (_install == null) {
            try {
                if (context == null)
                    throw new RuntimeException("UserBean.class ::: appContext is null !!!");
                _install = (QNTokenBean) LastingUtils.readObjectDES(context, qn_token_key);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return _install;
    }

    /**
     * 获取实例,如果有缓存系统将获取缓存中的对象
     *
     * @return
     */
    public static QNTokenBean getInstall(Activity activity) {
        Application appContext = null;
        if (activity != null)
            appContext = activity.getApplication();
        return getInstall(appContext);
    }

    /**
     * 保存
     *
     * @param context
     * @return
     */
    public boolean saveInfo(Context context) {
        boolean bol = LastingUtils.saveObjectDES(context, this, qn_token_key);
        if (bol) _install = this;
        return bol;
    }

    /**
     * 保存信息
     */
    public boolean saveInfo(Activity activity) {
        return saveInfo(activity.getApplication());
    }

    /**
     * 清空存储
     *
     * @param context
     * @return
     */
    public static boolean delete(Context context) {
        boolean bol = LastingUtils.delete(context, qn_token_key);
        if (bol) _install = null;
        return bol;
    }
}

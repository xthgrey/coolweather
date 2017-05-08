package com.coolweather.android.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by XTH on 2017/5/5.
 */

/**
 * 发送请求给服务器
 */
public class HttpUtil {
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient httpClient = new OkHttpClient();
        Request build = new Request.Builder().url(address).build();
        httpClient.newCall(build).enqueue(callback);
    }
}

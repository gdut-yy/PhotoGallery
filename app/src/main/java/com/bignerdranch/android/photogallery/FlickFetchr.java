package com.bignerdranch.android.photogallery;

import android.net.Uri;
import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 基本网络连接代码
 */
public class FlickFetchr {

    private static final String TAG = "FlickFetchr";

    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }
            int bytesRead = 0;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    public List<TouTiaoBean.ResultBean.DataBean> fetchItems( ) {

        List<TouTiaoBean.ResultBean.DataBean> items = new ArrayList<>();

        try {
            String url = Uri.parse("http://v.juhe.cn/toutiao/index?")
                    .buildUpon()
                    .appendQueryParameter("type", "top")
                    .appendQueryParameter("key", "4c71dccb365e8058f28093d02a7149ff")
                    .build().toString();

            String jsonString = getUrlString(url);
            Gson gson = new Gson();
            TouTiaoBean touTiaoBean = gson.fromJson(jsonString, TouTiaoBean.class);

            items = touTiaoBean.getResult().getData();

            // 打印 Log
            String json = gson.toJson(touTiaoBean);
            if (json.length() > 4000) {
                for (int i = 0; i < json.length(); i += 4000) {
                    if (i + 4000 < json.length())
                        Log.i("Gson" + i, json.substring(i, i + 4000));
                    else
                        Log.i("Gson" + i, json.substring(i, json.length()));
                }
            } else {
                Log.i("Gson", json);
            }

        } catch (IOException ioe) {
            Log.e(TAG, "Failed to fetch items", ioe);
        }
        return items;
    }
}

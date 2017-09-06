package com.thirty.java.newsapp;

/**
 * Created by zyj on 2017/9/5.
 */

import java.net.*;
import java.io.*;
import java.util.*;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;


public class NewsApiCaller {
    public static Bundle getLatestNews(int pageNo, int pageSize) throws Exception
    {
        String param = "?pageNo=" + pageNo + "&pageSize" + pageSize;
        URL url = new URL("http://166.111.68.66:2042/news/action/query/latest" + param);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5 * 1000);
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK)
        {
            InputStream is = conn.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String responseString = br.readLine();
            JSONObject newsInJson = new JSONObject(responseString);
            // now newsInJson holds the result
            JSONArray newsListInJson = newsInJson.getJSONArray("list");
            BriefNews[] briefNewsArray = new BriefNews[newsListInJson.length()];
            for (int i = 0; i < briefNewsArray.length; i++)
            {
                JSONObject jsonObject = newsListInJson.getJSONObject(i);
                briefNewsArray[i] = new BriefNews(jsonObject.getString("newsClassTag"),
                                                jsonObject.getString("news_Author"),
                                                jsonObject.getString("news_ID"),
                                                jsonObject.getString("news_Source"),
                                                jsonObject.getString("news_Pictures"),
                                                jsonObject.getString("news_Time"),
                                                jsonObject.getString("news_Title"),
                                                jsonObject.getString("news_URL"),
                                                jsonObject.getString("news_Intro"));
            }
            Bundle bundle = new Bundle();
            bundle.putParcelableArray("briefNewsArray", briefNewsArray);
            return bundle;
        }
        else
        { throw new Exception(); }
    }
}


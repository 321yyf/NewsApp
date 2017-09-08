package com.thirty.java.newsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by 321yy on 2017/9/7.
 */

public class NewsFragment extends Fragment {
    final String TYPE_RECOMMEND = "推荐";
    private RecyclerView rv;

    static public String[] categories = new String[]{
            "推荐", "科技", "教育", "军事", "国内",
            "社会", "文化", "汽车", "国际", "体育",
            "财经", "健康", "娱乐"
    };
    static public GetLatestNewsStream mNewsStream[];

    private LatestNewsDataDistributor mNewsDataDistributor = new LatestNewsDataDistributor();
    public MyAdapter mFragmentAdapter;

    // 用一个id标明，否则难以识别效果。
    private static final String ID = "id";
    public String mCategory;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            BriefNews[] briefNewsArray = (BriefNews[]) message.getData().getParcelableArray("briefNewsArray");
            Log.i("zyj", "length in handler: " + briefNewsArray.length);
            onReceiveNews(briefNewsArray);
        }
    };

    public void onReceiveNews(BriefNews[] briefNewsArray){
        Log.i("zyj", "mFragmentAdapter.mDataset before: " + mFragmentAdapter.mDataset.length);
        Log.i("zyj", "onReceiveNews: " + briefNewsArray.length);
        BriefNews[] tempArray = new BriefNews[mFragmentAdapter.mDataset.length + briefNewsArray.length];
        for (int i = 0; i < mFragmentAdapter.mDataset.length; i++)
            tempArray[i] = mFragmentAdapter.mDataset[i];
        for (int i = 0; i < briefNewsArray.length; i++)
            tempArray[mFragmentAdapter.mDataset.length + i] = briefNewsArray[i];
        mFragmentAdapter.mDataset = tempArray;
        Log.i("zyj", "mFragmentAdapter.mDataset after: " + mFragmentAdapter.mDataset.length);
        //mFragmentAdapter.mDataset = briefNewsArray;
        mFragmentAdapter.notifyDataSetChanged();
    }

    private Handler newsHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            DetailedNews detailedNews = (DetailedNews)message.getData().getParcelable("detailedNews");
            onReceiveDetailedNews(detailedNews);
        }
    };

    public void onReceiveDetailedNews(DetailedNews detailedNews){
        Intent intent = new Intent(getContext(), NewsActivity.class);
        intent.putExtra("News", detailedNews);
        this.startActivity(intent);
    }

    public static NewsFragment newInstance(int id) {
        NewsFragment f = new NewsFragment();
        Bundle b = new Bundle();
        b.putInt(ID, id);
        f.setArguments(b);
        return f;
    }

    public void refresh(){
        Log.i("yyf", "NewsFragment refresh " + mCategory);

        if (mFragmentAdapter != null) {
            if(rv != null){
                rv.setAdapter(mFragmentAdapter);
                Log.i("yyf", "NewsFragment refresh " + mCategory + "setAdapter");
            }

            mFragmentAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view , int position){
                    GetDetailedNewsRunnable runnable = new GetDetailedNewsRunnable(newsHandler, mFragmentAdapter.mDataset[position].newsID);
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            });

            if (mCategory != TYPE_RECOMMEND) {
                GetLatestNewsRunnable runnable = new GetLatestNewsRunnable(handler, 1, 10, mCategory);
                Thread thread = new Thread(runnable);
                thread.start();
            } else {
                //获取推荐

            }
            mFragmentAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rv = (RecyclerView) inflater.inflate(R.layout.my_index_view, container, false);

        if (mNewsStream == null){
            mNewsStream = new GetLatestNewsStream[13];
            for (int i = 1; i <= 12; ++i)
                mNewsStream[i] = new GetLatestNewsStream(categories[i]);
        }

        //加载新闻
        if (mFragmentAdapter == null) {
            mFragmentAdapter = new MyAdapter(new BriefNews[]{});
            rv.setAdapter(mFragmentAdapter);

            mFragmentAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view , int position){
                    GetDetailedNewsRunnable runnable = new GetDetailedNewsRunnable(newsHandler, mFragmentAdapter.mDataset[position].newsID);
                    Thread thread = new Thread(runnable);
                    thread.start();
                }
            });
            if (mCategory != TYPE_RECOMMEND) {
                //GetLatestNewsRunnable runnable = new GetLatestNewsRunnable(handler, 1, 10, mCategory);
                //Thread thread = new Thread(runnable);
                //thread.start();
                mNewsStream[NewsApiCaller.map.get(mCategory)].getNext(handler, 10);
            } else {
                //获取推荐
                double total_volume = 0;
                for (int i = 1; i <= 12; ++i)
                    total_volume += MyApplication.volumnOfCategory[i];

                double possibility[] = new double[13];
                for (int i = 1; i <= 12; ++i)
                    possibility[i] = MyApplication.volumnOfCategory[i] / total_volume;

                int count[] = new int[13];
                Arrays.fill(count, 0);
                for (int i = 1; i <= 10; ++i){
                    double corona  = Math.random();
                    for (int j = 1; j <= 12; ++j){
                        if (corona <= possibility[j] || j == 12){
                            ++count[j];
                            break;
                        }
                        corona -= possibility[j];
                    }
                }

                List<Pair<String, Integer>> mList = new ArrayList<Pair<String, Integer>>();

                for (int i = 1; i <= 12; ++i)
                    if (count[i] > 0) {
                        mList.add(new Pair<String, Integer>(categories[i], count[i]));
                    }
                mNewsDataDistributor.getNext(handler, mList);
            }
        }
        else
            rv.setAdapter(mFragmentAdapter);
        return rv;
    }
}

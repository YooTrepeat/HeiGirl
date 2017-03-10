package com.ct.heigirl.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ct.heigirl.R;
import com.ct.heigirl.bean.ResultBean;
import com.ct.heigirl.utils.LogUtils;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements AbsListView.OnScrollListener {

    private static final String TAG = "MainActivity";
    @InjectView(R.id.lv)
    ListView mLv;
    private Context mContext;
    private List<ResultBean.ResultsBean> datas;
    /**
     * _id : 58c1f808421aa95810795c34
     * createdAt : 2017-03-10T08:49:12.756Z
     * desc : 3-10
     * publishedAt : 2017-03-10T11:43:50.30Z
     * source : chrome
     * type : 福利
     * url : http://7xi8d6.com1.z0.glb.clouddn.com/2017-03-10-17127037_231706780569079_1119464847537340416_n.jpg
     * used : true
     * who : 代码家
     */

    private GirlAdapter adapter;
    private Gson mGson = new Gson();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        mContext = this;

        initView();
        initData();
        setListener();


    }

    private void initView() {

    }

    private void initData() {

        //初始化数据模型
        datas = new ArrayList<>();


        adapter = new GirlAdapter();
        mLv.setAdapter(adapter);

        /*new Thread(new Runnable() {
            @Override
            public void run() {
                getPhotoesSync();
            }
        }).start();*/
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPhotoesAsync();
            }
        }).start();

    }

    /**
     * 异步加载图片
     */
    private void getPhotoesAsync() {

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url + 1).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            //异步请求：不需要等待网络结果，就执行后面的代码，在子线程执行网络请求

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String result = response.body().string();
                ResultBean resultBean = mGson.fromJson(result, ResultBean.class);
                datas.addAll(resultBean.getResults());

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });

            }
        });
    }

    private String url = "http://gank.io/api/data/福利/10/";

    /**
     * 同步加载图片
     */
    private void getPhotoesSync() {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url + 1).build();
        try {
            Response response = client.newCall(request).execute();
//            LogUtils.d(TAG, response.body().string());
            String result = response.body().string();

            ResultBean resultBean = mGson.fromJson(result, ResultBean.class);

            datas.addAll(resultBean.getResults());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setListener() {
        mLv.setOnScrollListener(this);
    }

    /**
     * 滚动监听
     */
    private boolean isLoading;

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (scrollState == SCROLL_STATE_IDLE) {

            //当listview到达底部，加载下一页
            if (mLv.getLastVisiblePosition() == datas.size() - 1 && !isLoading) {

                loadMorePhotoes();
                LogUtils.d(TAG, "加载更多===" + datas.size() / 10 + ", size = " + datas.size());
            }
        }
    }


    //加载更过的图片，同步方式
    private void loadMorePhotoes() {

        isLoading = true;

        final String urlMore = url + datas.size() / 10 + 1;

        LogUtils.d(TAG, "请求的页数:" + (datas.size() / 10 + 1));

        new Thread(new Runnable() {
            @Override
            public void run() {

                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(urlMore).build();
                try {
                    Response response = client.newCall(request).execute();
//            LogUtils.d(TAG, response.body().string());
                    String result = response.body().string();

                    ResultBean resultBean = mGson.fromJson(result, ResultBean.class);

                    datas.addAll(resultBean.getResults());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();

                        }
                    });
                    isLoading = false;

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }


    private class GirlAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return datas.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = View.inflate(mContext, R.layout.item_list, null);

                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ResultBean.ResultsBean bean = datas.get(position);

            holder.tvPublish.setText(bean.getPublishedAt() + "位置:" + position);
            String url = bean.getUrl();
            Glide.with(mContext).load(url).centerCrop().into(holder.ivGirl);

            return convertView;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }
    }

    static class ViewHolder {
        ImageView ivGirl;
        TextView tvPublish;

        public ViewHolder(View root) {
            ivGirl = (ImageView) root.findViewById(R.id.iv_photo);
            tvPublish = (TextView) root.findViewById(R.id.tv_publish);
        }
    }
}

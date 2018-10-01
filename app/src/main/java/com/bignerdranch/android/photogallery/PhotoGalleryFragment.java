package com.bignerdranch.android.photogallery;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * PhotoGalleryFragment
 */
public class PhotoGalleryFragment extends Fragment {

    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private List<TouTiaoBean.ResultBean.DataBean> mItems = new ArrayList<>();
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloader;

    public static PhotoGalleryFragment newInstance( ) {
        return new PhotoGalleryFragment();
    }

    public PhotoGalleryFragment( ) {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        new FetchItemsTask().execute();

        Intent i = PollService.newIntent(getActivity());
        getActivity().startService(i);

        // 使用反馈 Handler
        Handler responseHandler = new Handler();
        mThumbnailDownloader = new ThumbnailDownloader<>(responseHandler);
        mThumbnailDownloader.setmThumbnailDownloadListener(new ThumbnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {
            @Override
            public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                target.bindDrawable(drawable);
            }
        });
        mThumbnailDownloader.start();
        mThumbnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
        mPhotoRecyclerView = (RecyclerView) view.findViewById(R.id.photo_recycler_view);
        mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        setupAdapter();

        return view;
    }

    @Override
    public void onDestroyView( ) {
        super.onDestroyView();
        mThumbnailDownloader.clearQueue();
    }

    @Override
    public void onDestroy( ) {
        super.onDestroy();
        mThumbnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    private void setupAdapter( ) {
        if (isAdded()) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    // AsyncTask 内部类
    private class FetchItemsTask extends AsyncTask<Void, Void, List<TouTiaoBean.ResultBean.DataBean>> {

        @Override
        protected List<TouTiaoBean.ResultBean.DataBean> doInBackground(Void... voids) {
//            try {
//                String result = new FlickFetchr().getUrlString("https://www.bignerdranch.com");
//                Log.i(TAG, "Fetched contents of URL: " + result);
//            } catch (IOException ioe) {
//                Log.i(TAG, "Failed to fetch URL: ", ioe);
//            }

            return new FlickFetchr().fetchItems();
        }

        @Override
        protected void onPostExecute(List<TouTiaoBean.ResultBean.DataBean> dataBeans) {
            mItems = dataBeans;
            setupAdapter();
        }
    }

    // ViewHolder 内部类
    private class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //
//        private TextView mTitleTextView;
        private ImageView mItemImageView;
        private TouTiaoBean.ResultBean.DataBean mDataBeanItem;

        public PhotoHolder(View itemView) {
            super(itemView);

//            mTitleTextView = (TextView) itemView;
            mItemImageView = (ImageView) itemView.findViewById(R.id.item_image_view);
            itemView.setOnClickListener(this);
        }

        //
//        public void bindGalleryItem(TouTiaoBean.ResultBean.DataBean dataBean) {
//            mTitleTextView.setText(dataBean.getTitle());
//        }
        public void bindDrawable(Drawable drawable) {
            mItemImageView.setImageDrawable(drawable);
        }

        public void bindGalleryItem(TouTiaoBean.ResultBean.DataBean dataBean) {
            mDataBeanItem = dataBean;
        }


        @Override
        public void onClick(View view) {
            Intent i = PhotoPageActivity.newIntent(getActivity(), Uri.parse(mDataBeanItem.getUrl()));
            startActivity(i);
        }
    }

    // Adapter 内部类
    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> {

        private List<TouTiaoBean.ResultBean.DataBean> mDataBeans;

        public PhotoAdapter(List<TouTiaoBean.ResultBean.DataBean> dataBeans) {
            mDataBeans = dataBeans;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            TextView textView = new TextView(getActivity());
//            return new PhotoHolder(textView);

            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_gallery, parent, false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            TouTiaoBean.ResultBean.DataBean dataBean = mDataBeans.get(position);
//            holder.bindGalleryItem(dataBean);
            holder.bindGalleryItem(dataBean);
            Drawable placeholder = getResources().getDrawable(R.drawable.bill_up_close);
            holder.bindDrawable(placeholder);
            mThumbnailDownloader.queueThumbnail(holder, dataBean.getThumbnail_pic_s());
        }


        @Override
        public int getItemCount( ) {
            return mDataBeans.size();
        }
    }

}

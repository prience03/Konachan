package com.ess.anime.wallpaper.glide;

import android.content.Context;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.signature.ObjectKey;
import com.ess.anime.wallpaper.bean.PoolListBean;

import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class PoolListModelLoader implements ModelLoader<PoolListBean, InputStream> {

    private Context mContext;

    PoolListModelLoader(Context context) {
        mContext = context;
    }

    @Nullable
    @Override
    public LoadData<InputStream> buildLoadData(@NonNull PoolListBean poolListBean, int width, int height, @NonNull Options options) {
        Key diskCacheKey = new ObjectKey(poolListBean.linkToShow);
        return new LoadData<>(diskCacheKey, new PoolListDataFetcher(mContext, poolListBean));
    }

    @Override
    public boolean handles(@NonNull PoolListBean poolListBean) {
        return true;
    }
}
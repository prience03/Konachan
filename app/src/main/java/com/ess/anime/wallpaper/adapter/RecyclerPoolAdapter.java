package com.ess.anime.wallpaper.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Priority;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.bean.PoolListBean;
import com.ess.anime.wallpaper.glide.GlideApp;
import com.ess.anime.wallpaper.glide.MyGlideModule;

import java.util.List;

public class RecyclerPoolAdapter extends BaseQuickAdapter<PoolListBean, BaseViewHolder> {

    public RecyclerPoolAdapter() {
        super(R.layout.recyclerview_item_pool);
    }

    @Override
    protected void convert(BaseViewHolder holder, PoolListBean poolListBean) {
        //缩略图
        Object imgUrl = TextUtils.isEmpty(poolListBean.thumbUrl)
                ? R.drawable.ic_placeholder_pool_no_cover
                : MyGlideModule.makeGlideUrl(poolListBean.thumbUrl);
        GlideApp.with(mContext)
                .load(imgUrl)
                .placeholder(R.drawable.ic_placeholder_pool)
                .priority(Priority.HIGH)
                .into((ImageView) holder.getView(R.id.iv_pool_thumb));

        //图集名称
        holder.setText(R.id.tv_name, poolListBean.name.replace("_", " "));

        //创建者
        String creator = TextUtils.isEmpty(poolListBean.creator)
                ? mContext.getString(R.string.unknown)
                : poolListBean.creator;
        holder.setText(R.id.tv_creator, creator);

        //图片数量
        holder.setGone(R.id.tv_post_count, !TextUtils.isEmpty(poolListBean.postCount));
        holder.setText(R.id.tv_post_count, poolListBean.postCount);

        //创建时间
        holder.setText(R.id.tv_create_time, poolListBean.createTime);

        //上传时间
        String update = TextUtils.isEmpty(poolListBean.updateTime)
                ? mContext.getString(R.string.unknown)
                : mContext.getString(R.string.pool_updated_time, poolListBean.updateTime);
        holder.setText(R.id.tv_update_time, update);
    }

    public boolean loadMoreDatas(List<PoolListBean> poolList) {
        return addDatas(mData.size(), poolList);
    }

    public boolean refreshDatas(List<PoolListBean> poolList) {
        return addDatas(0, poolList);
    }

    private boolean addDatas(int position, List<PoolListBean> poolList) {
        synchronized (this) {
            //删掉更新时因网站新增图片导致thumbList出现的重复项
            poolList.removeAll(mData);
            if (!poolList.isEmpty()) {
                addData(position, poolList);
                preloadThumbnail(poolList);
                return true;
            }
            return false;
        }
    }

    private void preloadThumbnail(List<PoolListBean> poolList) {
        try {
            for (PoolListBean poolListBean : poolList) {
                GlideApp.with(mContext)
                        .load(MyGlideModule.makeGlideUrl(poolListBean.thumbUrl))
                        .submit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

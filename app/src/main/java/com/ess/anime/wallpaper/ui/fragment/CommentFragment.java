package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerCommentAdapter;
import com.ess.anime.wallpaper.bean.CommentBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.HandlerFuture;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.ui.activity.ImageDetailActivity;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.SystemUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.ess.anime.wallpaper.website.WebsiteManager;

import org.jsoup.Jsoup;

import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;

public class CommentFragment extends BaseFragment {

    public final String TAG = CommentFragment.class.getName() + UUID.randomUUID().toString();

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.rv_comment)
    RecyclerView mRvComment;

    private ImageDetailActivity mActivity;
    private ThumbBean mThumbBean;
    private RecyclerCommentAdapter mCommentAdapter;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (ImageDetailActivity) context;
    }

    @Override
    int layoutRes() {
        return R.layout.fragment_comment;
    }

    @Override
    void init(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mThumbBean = savedInstanceState.getParcelable(Constants.THUMB_BEAN);
        } else {
            mThumbBean = mActivity.getThumbBean();
        }
        initView();
        initRecyclerView();
        getCommentList();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // 防止软件进入后台过久被系统回收导致切换回来时产生空指针异常
        outState.putParcelable(Constants.THUMB_BEAN, mThumbBean);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OkHttp.cancel(TAG);
    }

    private void initView() {
        mSwipeRefresh.setOnRefreshListener(this::getCommentList);
        mSwipeRefresh.setRefreshing(true);
    }

    private void initRecyclerView() {
        mRvComment.setLayoutManager(new LinearLayoutManager(mActivity));
        mCommentAdapter = new RecyclerCommentAdapter();
        mRvComment.setAdapter(mCommentAdapter);

        int spaceHor = UIUtils.dp2px(mActivity, 5);
        int spaceVer = UIUtils.dp2px(mActivity, 10);
        mRvComment.addItemDecoration(new GridDividerItemDecoration(
                1, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));
    }

    // 显示评论
    private void showComments(List<CommentBean> commentList) {
        if (mCommentAdapter.getEmptyView() == null) {
            mCommentAdapter.setEmptyView(getEmptyView());
        }
        mCommentAdapter.setNewData(commentList);
    }

    private View getEmptyView() {
        TextView tvEmpty = new TextView(mActivity);
        tvEmpty.setText(R.string.comment_no_comments);
        tvEmpty.setTextColor(ResourcesCompat.getColor(
                getResources(), R.color.color_text_unselected, null));
        tvEmpty.setTextSize(18);
        tvEmpty.setGravity(Gravity.CENTER);
        return tvEmpty;
    }

    // 获取评论列表
    private void getCommentList() {
        OkHttp.connect(mThumbBean.linkToShow, TAG, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure() {
                getCommentList();
            }

            @Override
            public void onSuccessful(String body) {
                HandlerFuture.ofWork(body)
                        .applyThen(body1 -> {
                            return WebsiteManager.getInstance()
                                    .getWebsiteConfig()
                                    .getHtmlParser()
                                    .getCommentList(Jsoup.parse(body1));
                        })
                        .runOn(HandlerFuture.IO.UI)
                        .applyThen(commentList -> {
                            setCommentList(commentList);
                        });
            }
        }, Request.Priority.IMMEDIATE);
    }

    // 获取到评论列表后刷新界面
    private void setCommentList(final List<CommentBean> commentList) {
        if (SystemUtils.isActivityActive(mActivity)) {
            showComments(commentList);
            mSwipeRefresh.setRefreshing(false);
        }
    }

}

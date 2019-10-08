package com.ess.anime.wallpaper.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.ess.anime.wallpaper.R;
import com.ess.anime.wallpaper.adapter.RecyclerPostAdapter;
import com.ess.anime.wallpaper.bean.ImageBean;
import com.ess.anime.wallpaper.bean.MsgBean;
import com.ess.anime.wallpaper.bean.ThumbBean;
import com.ess.anime.wallpaper.glide.MyGlideModule;
import com.ess.anime.wallpaper.global.Constants;
import com.ess.anime.wallpaper.http.HandlerFuture;
import com.ess.anime.wallpaper.http.OkHttp;
import com.ess.anime.wallpaper.http.parser.HtmlParser;
import com.ess.anime.wallpaper.http.parser.HtmlParserFactory;
import com.ess.anime.wallpaper.listener.DoubleTapEffector;
import com.ess.anime.wallpaper.model.helper.SoundHelper;
import com.ess.anime.wallpaper.ui.activity.MainActivity;
import com.ess.anime.wallpaper.ui.activity.SearchActivity;
import com.ess.anime.wallpaper.ui.view.CustomLoadMoreView;
import com.ess.anime.wallpaper.ui.view.GridDividerItemDecoration;
import com.ess.anime.wallpaper.utils.ComponentUtils;
import com.ess.anime.wallpaper.utils.FileUtils;
import com.ess.anime.wallpaper.utils.UIUtils;
import com.github.clans.fab.FloatingActionMenu;
import com.zyyoona7.popup.EasyPopup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;

public class PostFragment extends BaseFragment implements BaseQuickAdapter.RequestLoadMoreListener {

    public final static String TAG = PostFragment.class.getName();

    @BindView(R.id.tool_bar)
    Toolbar mToolbar;
    @BindView(R.id.floating_action_menu)
    FloatingActionMenu mFloatingMenu;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.rv_post)
    RecyclerView mRvPosts;

    private MainActivity mActivity;
    private StaggeredGridLayoutManager mLayoutManager;
    private RecyclerPostAdapter mPostAdapter;

    private EasyPopup mPopupPage;
    private TextView mTvFrom;
    private TextView mTvTo;
    private EditText mEtGoto;
    private int mCurrentPage;  // 当前页码
    private int mGoToPage;  // 跳转到的起始页码
    private String mCurrentTag;   // 当前正在搜索的tag
    private List<String> mCurrentTagList;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
    }

    @Override
    int layoutRes() {
        return R.layout.fragment_post;
    }

    @Override
    void init(Bundle savedInstanceState) {
        initToolBarLayout();
        initPopupPage();
        initSwipeRefreshLayout();
        initRecyclerView();
        mCurrentPage = 1;
        mGoToPage = 1;
        mCurrentTag = "";
        mCurrentTagList = new ArrayList<>();
        getNewPosts(mCurrentPage);
        changeFromPage(mCurrentPage);
        changeToPage(mCurrentPage);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        OkHttp.cancel(TAG);
        EventBus.getDefault().unregister(this);
    }

    private void initToolBarLayout() {
        mActivity.setSupportActionBar(mToolbar);
        DrawerLayout drawerLayout = mActivity.getDrawerLayout();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                mActivity,
                drawerLayout,
                mToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        mToolbar.setNavigationIcon(R.drawable.ic_nav_drawer);

        //双击返回顶部
        DoubleTapEffector.addDoubleTapEffect(mToolbar, () -> {
            scrollToTop();
            mFloatingMenu.close(true);
        });
    }

    // 切换图片显示方式（方格/瀑布流）
    @OnClick({R.id.iv_format})
    void toggleImageShownFormat() {
        boolean newFormat = !isPostImageShownRectangular();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.edit().putBoolean(Constants.IS_POST_IMAGE_SHOWN_RECTANGULAR, newFormat).apply();
        mPostAdapter.changeImageShownFormat(newFormat);
        scrollToTop();
        mFloatingMenu.close(true);
    }

    private boolean isPostImageShownRectangular() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return preferences.getBoolean(Constants.IS_POST_IMAGE_SHOWN_RECTANGULAR, true);
    }

    // 弹出跳转页弹窗
    @OnClick(R.id.iv_page)
    void gotoPage(View view) {
        mPopupPage.showAsDropDown(view);
        mEtGoto.selectAll();
        mEtGoto.post(() -> UIUtils.showSoftInput(mActivity, mEtGoto));
    }

    //搜索
    @OnClick({R.id.iv_search})
    void openSearch() {
        mFloatingMenu.close(true);
        Intent searchIntent = new Intent(mActivity, SearchActivity.class);
        startActivityForResult(searchIntent, Constants.SEARCH_CODE);
    }

    private void initPopupPage() {
        mPopupPage = EasyPopup.create()
                .setContentView(mActivity, R.layout.layout_popup_goto_page)
                .setFocusAndOutsideEnable(true)
                .setBackgroundDimEnable(true)
                .setDimValue(0.4f)
                .apply();

        // 当前显示的起始页与终止页
        mTvFrom = mPopupPage.findViewById(R.id.tv_from);
        mTvTo = mPopupPage.findViewById(R.id.tv_to);

        // 页码跳转
        mEtGoto = mPopupPage.findViewById(R.id.et_goto);
        mEtGoto.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_GO) {
                String num = mEtGoto.getText().toString();
                if (!TextUtils.isEmpty(num)) {
                    int newPage = Integer.parseInt(num);
                    if (newPage > 0) {
                        resetAll(newPage);
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                    }
                }
                mPopupPage.dismiss();
            }
            return false;
        });
    }

    private void initSwipeRefreshLayout() {
        mSwipeRefresh.setRefreshing(true);
        //下拉刷新
        mSwipeRefresh.setOnRefreshListener(() -> {
            getNewPosts(mGoToPage);
            if (mPostAdapter.getData().isEmpty()) {
                mPostAdapter.setEmptyView(R.layout.layout_loading_cirno, mRvPosts);
            }
        });
    }

    private void initRecyclerView() {
        int span = Math.max(UIUtils.px2dp(getContext(), UIUtils.getScreenWidth(getContext())) / 165, 2);
        mLayoutManager = new StaggeredGridLayoutManager(span, StaggeredGridLayoutManager.VERTICAL);
        mRvPosts.setLayoutManager(mLayoutManager);

        mPostAdapter = new RecyclerPostAdapter(TAG);
        mPostAdapter.bindToRecyclerView(mRvPosts);
        mPostAdapter.setOnItemClickListener(() -> mFloatingMenu.close(true));
        mPostAdapter.setOnLoadMoreListener(this, mRvPosts);
        mPostAdapter.setPreLoadNumber(10);
        mPostAdapter.setLoadMoreView(new CustomLoadMoreView());
        mPostAdapter.setEmptyView(R.layout.layout_loading_cirno, mRvPosts);
        mPostAdapter.changeImageShownFormat(isPostImageShownRectangular());

        int spaceHor = UIUtils.dp2px(mActivity, 5);
        int spaceVer = UIUtils.dp2px(mActivity, 10);
        mRvPosts.addItemDecoration(new GridDividerItemDecoration(
                span, GridDividerItemDecoration.VERTICAL, spaceHor, spaceVer, true));

        // 滑动时隐藏fab
        mRvPosts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!mPostAdapter.getData().isEmpty()) {
                    switch (newState) {
                        case RecyclerView.SCROLL_STATE_IDLE:
                            mFloatingMenu.showMenu(true);
                            break;
                        case RecyclerView.SCROLL_STATE_DRAGGING:
                            mFloatingMenu.hideMenu(true);
                            break;
                    }
                }
            }
        });
    }

    // 滑动加载更多
    @Override
    public void onLoadMoreRequested() {
        String url = OkHttp.getPostUrl(mActivity, ++mCurrentPage, mCurrentTagList);
        OkHttp.connect(url, TAG, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure() {
                checkNetwork();
            }

            @Override
            public void onSuccessful(String body) {
                HandlerFuture.ofWork(body)
                        .applyThen(body1 -> {
                            return HtmlParserFactory.createParser(mActivity, body1).getThumbList();
                        })
                        .runOn(HandlerFuture.IO.UI)
                        .applyThen(thumbList -> {
                            addMoreThumbList(thumbList);
                        });

            }
        }, Request.Priority.IMMEDIATE);
    }

    //加载更多完成后刷新界面
    private void addMoreThumbList(final List<ThumbBean> newList) {
        if (!mPostAdapter.isLoading()) {
            return;
        }

        mPostAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPosts);
        if (mPostAdapter.loadMoreDatas(newList)) {
            mPostAdapter.loadMoreComplete();
            changeToPage(mCurrentPage);
        } else {
            mPostAdapter.loadMoreEnd();
        }
    }

    @OnClick(R.id.fab_home)
    void searchHome() {
        mFloatingMenu.close(true);
        onActivityResult(Constants.SEARCH_CODE, Constants.SEARCH_CODE_HOME, new Intent());
    }

    @OnClick(R.id.fab_random)
    void searchRandom() {
        mFloatingMenu.close(true);
        if (TextUtils.equals(OkHttp.getBaseUrl(mActivity), Constants.BASE_URL_GELBOORU)) {
            Toast.makeText(mActivity, R.string.cannot_search_random, Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent();
            intent.putExtra(Constants.SEARCH_TAG, "order:random");
            onActivityResult(Constants.SEARCH_CODE, Constants.SEARCH_CODE_RANDOM, intent);
        }
    }

    private void changeFromPage(int page) {
        mTvFrom.setText(String.valueOf(page));
    }

    private void changeToPage(int page) {
        mTvTo.setText(String.valueOf(page));
    }

    private void scrollToTop() {
        int smoothPos = 7 * mLayoutManager.getSpanCount();
        int lastVisiblePos = mLayoutManager.findLastVisibleItemPositions(null)[0];
        if (lastVisiblePos > smoothPos) {
            mRvPosts.scrollToPosition(smoothPos);
        }
        mRvPosts.smoothScrollToPosition(0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.SEARCH_CODE) {
            if (data != null) {
                resetAll(1);
                mCurrentTagList.clear();

                String searchTag = data.getStringExtra(Constants.SEARCH_TAG);
                mCurrentTag = "#" + searchTag;
                switch (resultCode) {
                    case Constants.SEARCH_CODE_TAGS:
                        mCurrentTagList.addAll(Arrays.asList(searchTag.split("[,，]")));
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_ID:
                        mCurrentTag = "#id:" + searchTag;
                        mCurrentTagList.add("id:" + searchTag);
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_CHINESE:
                        getNameFromBaidu(searchTag);
                        break;
                    case Constants.SEARCH_CODE_ADVANCED:
                        String[] tags = searchTag.split(" ");
                        mCurrentTagList.addAll(Arrays.asList(tags));
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_HOME:
                        mCurrentTag = "";
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                        break;
                    case Constants.SEARCH_CODE_RANDOM:
                        mCurrentTagList.add(searchTag);
                        getNewPosts(mCurrentPage);
                        changeFromPage(mCurrentPage);
                        changeToPage(mCurrentPage);
                        break;
                }
            }
        }
    }


    /**
     * 初始化所有数据，清空adapter，以便加载新内容
     *
     * @param startPage 加载的起始页
     */
    private void resetAll(int startPage) {
        OkHttp.cancel(TAG);
        mPostAdapter.setEmptyView(R.layout.layout_loading_cirno, mRvPosts);
        mPostAdapter.setNewData(null);
        mSwipeRefresh.setRefreshing(true);
        mCurrentPage = startPage;
        mGoToPage = startPage;
    }

    private void getNewPosts(int page) {
        String url = OkHttp.getPostUrl(mActivity, page, mCurrentTagList);
        OkHttp.connect(url, TAG, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure() {
                checkNetwork();
            }

            @Override
            public void onSuccessful(String body) {
                HandlerFuture.ofWork(body)
                        .applyThen(body1 -> {
                            return HtmlParserFactory.createParser(mActivity, body1).getThumbList();
                        })
                        .runOn(HandlerFuture.IO.UI)
                        .applyThen(thumbList -> {
                            refreshThumbList(thumbList);
                        });
            }
        }, Request.Priority.IMMEDIATE);
    }

    //搜索新内容或下拉刷新完成后刷新界面
    private void refreshThumbList(final List<ThumbBean> newList) {
        if (!mSwipeRefresh.isRefreshing()) {
            return;
        }

        mPostAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPosts);
        if (mPostAdapter.refreshDatas(newList)) {
            scrollToTop();
        } else if (mPostAdapter.getData().isEmpty()) {
            loadNothing();
        }
        mSwipeRefresh.setRefreshing(false);
    }

    // 根据汉语从百度百科搜索对应罗马音
    private void getNameFromBaidu(String searchTag) {
        String url = Constants.BASE_URL_BAIDU + searchTag;
        OkHttp.connect(url, TAG, new OkHttp.OkHttpCallback() {
            @Override
            public void onFailure() {
                checkNetwork();
            }

            @Override
            public void onSuccessful(String body) {
                String name = HtmlParser.getNameFromBaidu(body);
                if (!TextUtils.isEmpty(name)) {
                    String tag1 = "~" + name;
                    mCurrentTagList.add(tag1);

                    String[] split = name.split("_");
                    StringBuilder tag2 = new StringBuilder("~");
                    for (int i = split.length - 1; i >= 0; i--) {
                        tag2.append(split[i]).append("_");
                    }
                    tag2.replace(tag2.length() - 1, tag2.length(), "");
                    mCurrentTagList.add(tag2.toString());
                    getNewPosts(mCurrentPage);
                    changeFromPage(mCurrentPage);
                    changeToPage(mCurrentPage);
                } else {
                    loadNothing();
                }
            }
        }, Request.Priority.IMMEDIATE);
    }

    //获取到图片详细信息后收到的通知，obj 为 Json (String)
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void setImageBean(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.GET_IMAGE_DETAIL)) {
            String json = (String) msgBean.obj;
            ImageBean imageBean = ImageBean.getImageDetailFromJson(json);
            List<ThumbBean> thumbList = mPostAdapter.getData();
            for (ThumbBean thumbBean : thumbList) {
                if (thumbBean.checkImageBelongs(imageBean)) {
                    if (thumbBean.imageBean == null) {
                        thumbBean.imageBean = imageBean;
                        thumbBean.checkToReplacePostData();
                        String url = imageBean.posts[0].sampleUrl;
                        if (FileUtils.isImageType(url) && ComponentUtils.isActivityActive(mActivity)) {
                            MyGlideModule.preloadImage(mActivity, url);
                        }
                    }
                    break;
                }
            }
        }
    }

    //百度或K站搜所无结果
    private void loadNothing() {
        mPostAdapter.setEmptyView(R.layout.layout_load_nothing, mRvPosts);
        mPostAdapter.setNewData(null);
        mSwipeRefresh.setRefreshing(false);
        SoundHelper.getInstance().playLoadNothingSound(getActivity());
    }

    //访问网络失败
    private void checkNetwork() {
        mSwipeRefresh.setRefreshing(false);
        if (mPostAdapter.getData().isEmpty()) {
            mPostAdapter.setEmptyView(R.layout.layout_load_no_network, mRvPosts);
            SoundHelper.getInstance().playLoadNoNetworkSound(getActivity());
        } else {
            mPostAdapter.loadMoreFail();
        }
    }

    //切换搜图网站后收到的通知，obj 为 null
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void changeBaseUrl(MsgBean msgBean) {
        if (msgBean.msg.equals(Constants.CHANGE_BASE_URL)) {
            resetAll(1);
            getNewPosts(mCurrentPage);
            changeFromPage(mCurrentPage);
            changeToPage(mCurrentPage);
        }
    }

    public static PostFragment newInstance() {
        PostFragment fragment = new PostFragment();
        return fragment;
    }
}

package com.ess.anime.wallpaper.ui.activity;

import com.ess.anime.wallpaper.R;

public class SauceNaoActivity extends BaseWebActivity {

    @Override
    int titleRes() {
        return R.string.nav_sauce_nao;
    }

    @Override
    String webUrl() {
        return "https://saucenao.com/";
    }

}

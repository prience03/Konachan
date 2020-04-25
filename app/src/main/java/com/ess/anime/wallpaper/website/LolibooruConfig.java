package com.ess.anime.wallpaper.website;

import com.ess.anime.wallpaper.http.parser.GeneralParser;

import java.util.ArrayList;
import java.util.List;

public class LolibooruConfig extends WebsiteConfig<GeneralParser> {

    @Override
    public String getBaseUrl() {
        return BASE_URL_LOLIBOORU;
    }

    @Override
    public String getTagJsonUrl() {
        return TAG_JSON_URL_LOLIBOORU;
    }

    @Override
    public String getPostUrl(int page, List<String> tagList) {
        if (tagList == null) {
            tagList = new ArrayList<>();
        }

        StringBuilder tags = new StringBuilder();
        for (String tag : tagList) {
            tags.append(tag).append("+");
        }

        return getBaseUrl() + "post?page=" + page + "&tags=" + tags;
    }

    @Override
    public boolean hasPool() {
        return true;
    }

    @Override
    public String getPoolUrl(int page, String name) {
        name = name == null ? "" : name;
        return getBaseUrl() + "pool?commit=Search&page=" + page + "&query=" + name;
    }

    @Override
    public String getPoolPostUrl(String linkToShow, int page) {
        return linkToShow + "?page=" + page;
    }

    @Override
    public String getSavedImageHead() {
        return "Lolibooru-";
    }

    @Override
    public boolean isSupportRandomPost() {
        return true;
    }

    @Override
    public boolean isSupportAdvancedSearch() {
        return true;
    }

}

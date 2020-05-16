package com.ess.anime.wallpaper.http;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import androidx.annotation.Nullable;

public class PriorityStringRequest extends StringRequest {

    private Priority mPriority;
    private Map<String, String> mHeaderMap = new LinkedHashMap<>();

    public PriorityStringRequest(int method, String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public PriorityStringRequest(String url, Response.Listener<String> listener, @Nullable Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    @Override
    public Priority getPriority() {
        return mPriority;
    }

    public void setPriority(Priority priority) {
        mPriority = priority;
    }

    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        // 解决中文乱码问题
        String parsed = new String(response.data, StandardCharsets.UTF_8);
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    public void addHeaders(Map<String, String> headerMap) {
        mHeaderMap.putAll(headerMap);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return super.getParams();
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_2) AppleWebKit / 537.36(KHTML, like Gecko) Chrome  47.0.2526.106 Safari / 537.36");
        headers.putAll(mHeaderMap);
        return headers;
    }

}

package com.honeycomb.colorphone.http.lib.call;

public interface Callback<T> {

    void onFailure(String errorMsg);

    void onSuccess(T t);
}

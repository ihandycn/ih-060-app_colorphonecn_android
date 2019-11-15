package com.honeycomb.colorphone.http.lib.call;

public interface RealCallback<T> {

    void onSuccess(T t);

    void onFailure(String errorMsg);
}

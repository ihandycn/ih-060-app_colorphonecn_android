package com.honeycomb.colorphone.news;

class DislikeInfo {
    int type;
    int code;
    String msg;

    @Override public String toString() {
        return "DislikeInfo{" +
                "type=" + type +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }
}

package com.honeycomb.colorphone.customize.util;


import com.honeycomb.colorphone.ICustomizeService;

/**
 * Activities should implement this as declaration of their responsibility to hold
 * {@link ICustomizeService}.
 */
public interface ServiceHolder {

    ICustomizeService getService();
}

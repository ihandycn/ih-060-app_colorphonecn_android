package com.honeycomb.colorphone.customize.util;


import com.honeycomb.colorphone.ICustomizeService;

/**
 * Implemented by views and adapters that needs to be notified when {@link ICustomizeService} is connected.
 *
 * {@link ICustomizeService} is for IPC to do operations that can only be performed from the main process.
 */
public interface ServiceListener {

    void onServiceConnected(ICustomizeService service);
}

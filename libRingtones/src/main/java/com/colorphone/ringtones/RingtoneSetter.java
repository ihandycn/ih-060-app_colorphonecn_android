package com.colorphone.ringtones;

import com.colorphone.ringtones.module.Ringtone;

public interface RingtoneSetter {
    void generateFilePath(Ringtone ringtone);
    boolean onSetAsDefault(Ringtone ringtone);
    boolean onSetForSomeOne(Ringtone ringtone);
}

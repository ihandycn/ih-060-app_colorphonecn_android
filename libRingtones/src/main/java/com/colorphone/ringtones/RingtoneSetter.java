package com.colorphone.ringtones;

import com.colorphone.ringtones.module.Ringtone;

public interface RingtoneSetter {
    boolean onSetAsDefault(Ringtone ringtone);
    boolean onSetForSomeOne(Ringtone ringtone);
}

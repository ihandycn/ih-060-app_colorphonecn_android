package com.colorphone.ringtones;

import com.colorphone.ringtones.module.Ringtone;

public interface RingtoneSetter {
    boolean onSetRingtone(Ringtone ringtone);
    boolean onSetAsDefault(Ringtone ringtone);
    boolean onSetForSomeOne(Ringtone ringtone);
}

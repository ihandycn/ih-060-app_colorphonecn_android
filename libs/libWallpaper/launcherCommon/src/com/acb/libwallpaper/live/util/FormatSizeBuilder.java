package com.acb.libwallpaper.live.util;


import android.text.TextUtils;

 import com.honeycomb.colorphone.R;
import com.ihs.app.framework.HSApplication;

import java.util.Locale;

public class FormatSizeBuilder {

    public String size;
    public String unit;
    public String sizeUnit;

    public FormatSizeBuilder(long sizeBytes) {
        init(sizeBytes);
    }

    public FormatSizeBuilder(long sizeBytes, boolean formatBadgeSize) {
        if (formatBadgeSize) {
            int unitResid = R.string.megabyte_abbr;
            double result = sizeBytes;
            if (result < 1024) {
                result = 0;
            }
            if (result >= 1024) {
                unitResid = R.string.kilobyte_abbr;
                result = result / 1024;
                size = String.valueOf(Math.round(result));
            }
            if (result >= 1024) {
                unitResid = R.string.megabyte_abbr;
                result = result / 1024;
                size = String.valueOf(Math.round(result));
            }
            if (result > 1000) {
                unitResid = R.string.gigabyte_abbr;
                result = result / 1024;
                size = String.format(Locale.US, "%.2f", result);
            }
            if (TextUtils.isEmpty(size)) {
                size = "0";
            }
            this.unit = HSApplication.getContext().getString(unitResid);
            this.sizeUnit = size + " " + unit;
        } else {
            init(sizeBytes);
        }
    }

    private void init(long sizeBytes) {
        int unitResid = R.string.byte_abbr;
        double result = sizeBytes;
        if (result > 900) {
            unitResid = R.string.kilobyte_abbr;
            result = result / 1024;
        }
        if (result > 900) {
            unitResid = R.string.megabyte_abbr;
            result = result / 1024;
        }
        if (result > 900) {
            unitResid = R.string.gigabyte_abbr;
            result = result / 1024;
        }
        if (result > 900) {
            unitResid = R.string.terabyte_abbr;
            result = result / 1024;
        }
        if (result > 900) {
            unitResid = R.string.petabyte_abbr;
            result = result / 1024;
        }
        if (result == 0) {
            size = "0";
        } else if (result < 10) {
            size = String.format(Locale.US, "%.2f", result);
        } else if (result < 100) {
            size = String.format(Locale.US, "%.1f", result);
        } else {
            size = String.format(Locale.US, "%.0f", result);
        }

        this.unit = HSApplication.getContext().getString(unitResid);
        this.sizeUnit = size + " " + unit;
    }

}

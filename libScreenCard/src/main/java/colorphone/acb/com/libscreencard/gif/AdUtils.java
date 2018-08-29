package colorphone.acb.com.libscreencard.gif;

import com.ihs.commons.utils.HSLog;

import net.appcloudbox.ads.base.AcbAd;

public class AdUtils {
    public static boolean isFacebookAd(AcbAd ad) {
        HSLog.d(AdUtils.class.getSimpleName(), "vendor name is " + ad.getVendorConfig().name());
        return ad != null && ad.getVendorConfig().name().toLowerCase().startsWith("facebook");
    }
}


package colorphone.acb.com.libweather.view.recyclerview;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;

/**
 * Disable predictive animations. There is a bug in RecyclerView which causes views that
 * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
 * adapter size has decreased since the ViewHolder was recycled.
 */
public class SafeLinearLayoutManager extends LinearLayoutManager {

    public SafeLinearLayoutManager(Context context) {
        super(context);
    }

    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }
}

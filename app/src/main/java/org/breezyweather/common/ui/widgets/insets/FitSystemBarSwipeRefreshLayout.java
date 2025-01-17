package org.breezyweather.common.ui.widgets.insets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowInsets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.breezyweather.common.basic.insets.FitBothSideBarHelper;
import org.breezyweather.common.basic.insets.FitBothSideBarView;
import org.breezyweather.R;

public class FitSystemBarSwipeRefreshLayout extends SwipeRefreshLayout
        implements FitBothSideBarView {

    private final FitBothSideBarHelper mHelper;

    public FitSystemBarSwipeRefreshLayout(@NonNull Context context) {
        this(context, null);
    }

    public FitSystemBarSwipeRefreshLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mHelper = new FitBothSideBarHelper(this, SIDE_TOP);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        return mHelper.onApplyWindowInsets(insets, this::fitSystemBar);
    }

    private void fitSystemBar() {
        int startPosition = mHelper.top() + getResources().getDimensionPixelSize(R.dimen.normal_margin);
        int endPosition = (int) (startPosition + 64 * getResources().getDisplayMetrics().density);

        if (startPosition != getProgressViewStartOffset()
                || endPosition != getProgressViewEndOffset()) {
            setProgressViewOffset(false, startPosition, endPosition);
        }
    }

    @Override
    public void addFitSide(@FitSide int side) {
        // do nothing.
    }

    @Override
    public void removeFitSide(@FitSide int side) {
        // do nothing.
    }

    @Override
    public void setFitSystemBarEnabled(boolean top, boolean bottom) {
        mHelper.setFitSystemBarEnabled(top, bottom);
    }

    @Override
    public int getTopWindowInset() {
        return mHelper.top();
    }

    @Override
    public int getBottomWindowInset() {
        return 0;
    }
}

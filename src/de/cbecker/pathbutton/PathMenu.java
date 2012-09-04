
package de.cbecker.pathbutton;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

@SuppressLint("ViewConstructor")
public class PathMenu extends RelativeLayout implements View.OnClickListener {

    public static interface OnSubMenuListener {

        public void onSubMenuEvent(PathMenuEntry entry);

    }

    public static enum Position {

        TopLeft(1, 1), TopRight(-1, 1), BottomRight(-1, -1), BottomLeft(1, -1);

        private final int mSignX;

        private final int mSignY;

        private Position(int signX, int signY) {
            mSignX = signX;
            mSignY = signY;
        }

        public int getSignX() {
            return mSignX;
        }

        public int getSignY() {
            return mSignY;
        }

    }

    private static final double sArcLength = Math.PI * 2 / 360;

    private final Position mPosition;

    private final OnSubMenuListener mSubMenuListener;

    private final float mDegreeGap;

    private final int mOffsetX;

    private final int mOffsetY;

    private final ImageButton mMenuButton;

    private final ArrayList<TextView> mSubMenuButtons;

    private boolean mOpen = false;

    public PathMenu(Context context, Position position, ArrayList<PathMenuEntry> subMenuEntries,
            OnSubMenuListener subMenuListener) {
        super(context);

        mPosition = position;
        mSubMenuListener = subMenuListener;

        Resources res = getResources();
        mDegreeGap = res.getDimension(R.dimen.degrees) / subMenuEntries.size();
        int offset = res.getDimensionPixelOffset(R.dimen.offset);
        mOffsetX = mPosition.getSignX() * offset;
        mOffsetY = mPosition.getSignY() * offset;

        mMenuButton = new ImageButton(getContext());
        mMenuButton.setBackgroundResource(R.drawable.bg_button_menu);
        mMenuButton.setOnClickListener(this);
        addView(mMenuButton, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        mSubMenuButtons = new ArrayList<TextView>();
        for (int i = 0; i < subMenuEntries.size(); ++i) {
            TextView tv = new TextView(getContext());
            tv.setTag(subMenuEntries.get(i));
            tv.setText(subMenuEntries.get(i).getName());
            tv.setTextColor(Color.BLACK);
            tv.setBackgroundResource(R.drawable.bg_button_submenu);
            tv.setGravity(Gravity.CENTER);
            tv.setOnClickListener(this);
            tv.setVisibility(INVISIBLE);

            mSubMenuButtons.add(tv);

            addView(tv, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        }

        mMenuButton.bringToFront();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

        LayoutParams params = (LayoutParams)mMenuButton.getLayoutParams();
        switch (mPosition) {
            case TopLeft:
                params.topMargin = 0;
                params.leftMargin = 0;
                break;
            case TopRight:
                params.topMargin = 0;
                params.leftMargin = getWidth() - mMenuButton.getWidth();
                break;
            case BottomRight:
                params.topMargin = getHeight() - mMenuButton.getHeight();
                params.leftMargin = getWidth() - mMenuButton.getWidth();
                break;
            case BottomLeft:
                params.topMargin = getHeight() - mMenuButton.getHeight();
                params.leftMargin = 0;
                break;
        }
        mMenuButton.setLayoutParams(params);

        int centerX = params.leftMargin + mMenuButton.getWidth() / 2;
        int centerY = params.topMargin + mMenuButton.getHeight() / 2;

        for (int i = 0; i < mSubMenuButtons.size(); ++i) {
            int x = centerX + (int)getXPositionOnCircle(i);
            int y = centerY + (int)getYPositionOnCircle(i);

            params = (LayoutParams)mSubMenuButtons.get(i).getLayoutParams();
            params.topMargin = y - mSubMenuButtons.get(i).getHeight() / 2;
            params.leftMargin = x - mSubMenuButtons.get(i).getWidth() / 2;

            mSubMenuButtons.get(i).setLayoutParams(params);
        }
    }

    private void startSubMenuMove() {
        mOpen = true;
        mMenuButton.startAnimation(getRotateOpenAnimation());

        int time = 400;
        for (int i = 0; i < mSubMenuButtons.size(); ++i) {
            mSubMenuButtons.get(i).setVisibility(VISIBLE);
            mSubMenuButtons.get(i).clearAnimation();

            float x = getXPositionOnCircle(i);
            float y = getYPositionOnCircle(i);

            mSubMenuButtons.get(i).startAnimation(getMoveOutAnimation(-x, 0, -y, 0, time));

            time -= 50;
        }
    }

    private void removeSubMenuMove() {
        mOpen = false;
        mMenuButton.startAnimation(getRotateCloseAnimation());

        int time = 300;
        for (int i = 0; i < mSubMenuButtons.size(); i++) {
            mSubMenuButtons.get(i).clearAnimation();

            float x = getXPositionOnCircle(i);
            float y = getYPositionOnCircle(i);

            mSubMenuButtons.get(i).startAnimation(getMoveInAnimation(0, -x, 0, -y, time));
            mSubMenuButtons.get(i).setVisibility(INVISIBLE);

            time -= 50;
        }
    }

    private Animation getRotateOpenAnimation() {
        Animation rotate = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setRepeatCount(0);
        rotate.setDuration(250);
        rotate.setInterpolator(new AccelerateInterpolator());
        rotate.setRepeatMode(Animation.ABSOLUTE);
        return rotate;
    }

    private Animation getRotateCloseAnimation() {
        Animation rotate = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(250);
        rotate.setInterpolator(new AccelerateInterpolator());
        return rotate;
    }

    private Animation getMoveOutAnimation(float fromXDelta, float toXDelta, float fromYDelta,
            float toYDelta, int timer) {
        Animation translate = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
        translate.setDuration(timer);
        translate.setInterpolator(new AccelerateInterpolator());
        return translate;
    }

    private Animation getMoveInAnimation(float fromXDelta, float toXDelta, float fromYDelta,
            float toYDelta, int timer) {
        Animation translate = new TranslateAnimation(fromXDelta, toXDelta, fromYDelta, toYDelta);
        translate.setDuration(timer);
        translate.setInterpolator(new AccelerateInterpolator());
        return translate;
    }

    @Override
    public void onClick(View v) {
        if (v == mMenuButton) {
            if (!mOpen) {
                startSubMenuMove();
            } else {
                removeSubMenuMove();
            }
        } else {
            for (int i = 0; i < mSubMenuButtons.size(); i++) {
                if (v == mSubMenuButtons.get(i)) {
                    removeSubMenuMove();

                    /* Notificate the listener about the SubMenuEvent */
                    mSubMenuListener.onSubMenuEvent((PathMenuEntry)mSubMenuButtons.get(i).getTag());

                    break;
                }
            }
        }
    }

    private float getXPositionOnCircle(int position) {
        double x = 100 * Math.cos(sArcLength * (mDegreeGap * position + mPosition.ordinal() * 90));
        return mOffsetX + (float)x;
    }

    private float getYPositionOnCircle(int position) {
        double y = 100 * Math.sin(sArcLength * (mDegreeGap * position + mPosition.ordinal() * 90));
        return mOffsetY + (float)y;
    }

}

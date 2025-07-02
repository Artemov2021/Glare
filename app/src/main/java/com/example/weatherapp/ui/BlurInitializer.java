package com.example.weatherapp.ui;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;

import eightbitlab.com.blurview.BlurView;

public class BlurInitializer {
    public static void initBlurView(Activity activity, BlurView blurView, ViewGroup rootLayout, float blurRadiusDp, float cornerRadiusDp) {
        Drawable windowBackground = activity.getWindow().getDecorView().getBackground();

        blurView.setupWith(rootLayout)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(blurRadiusDp);

        int cornerRadiusPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                cornerRadiusDp,
                activity.getResources().getDisplayMetrics()
        );

        blurView.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), cornerRadiusPx);
            }
        });
        blurView.setClipToOutline(true);
    }
    public static void setStroke(View view, float strokeWidthDp, int strokeColor, float opacity) {
        float density = view.getResources().getDisplayMetrics().density;
        int strokeWidthPx = (int) (strokeWidthDp * density);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT); // keep BlurView content visible
        drawable.setStroke(strokeWidthPx, adjustAlpha(strokeColor, opacity));
        drawable.setCornerRadius(23 * density); // match your blurView rounding

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        } else {
            view.setBackgroundDrawable(drawable);
        }
    }
    private static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }
}

package com.goodiebag.pinview.Utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by pavan on 11/01/17.
 */

public class Utils {

    public static int dpToPx(Context c, int dp) {
        DisplayMetrics displayMetrics = c.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

}

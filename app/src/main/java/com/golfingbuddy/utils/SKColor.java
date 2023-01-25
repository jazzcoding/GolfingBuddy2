package com.golfingbuddy.utils;


import android.graphics.Color;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by kairat on 11/18/14.
 */
public class SKColor {
    public static int RGBtoColor(String input) {
        Pattern pattern = Pattern.compile("rgb *\\( *([0-9]+), *([0-9]+), *([0-9]+) *\\)");
        Matcher matcher = pattern.matcher(input);

        if (matcher.matches())
        {
            return Color.rgb(Integer.parseInt(matcher.group(1)),  // r
                             Integer.parseInt(matcher.group(2)),  // g
                             Integer.parseInt(matcher.group(3))); // b
        }

        return Color.TRANSPARENT;
    }
}

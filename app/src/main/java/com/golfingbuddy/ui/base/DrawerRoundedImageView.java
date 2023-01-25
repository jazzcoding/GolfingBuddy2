package com.golfingbuddy.ui.base;

/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.golfingbuddy.utils.SKDimensions;
import com.golfingbuddy.utils.SKImageView;




public class DrawerRoundedImageView extends SKImageView {

    private static int HARDCODED_WIDTH_IN_DP = 50;
    private static int HARDCODED_BORDER_WIDTH_IN_DP = 3;

    public DrawerRoundedImageView(Context context) {
        super(context);
    }

    public DrawerRoundedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawerRoundedImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable drawable = getDrawable();

        if (drawable == null) {
            return;
        }

        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

        int w = getWidth(), h = getHeight(), size = SKDimensions.convertDpToPixel(DrawerRoundedImageView.HARDCODED_WIDTH_IN_DP, getContext());

        bitmap = bitmap.createScaledBitmap(bitmap, size, size, true);
        Bitmap roundBitmap = getRoundedCornerBitmap(bitmap, w);

        Bitmap bmpWithBorder = Bitmap.createBitmap(roundBitmap.getWidth() + DrawerRoundedImageView.HARDCODED_BORDER_WIDTH_IN_DP*2, roundBitmap.getHeight() + DrawerRoundedImageView.HARDCODED_BORDER_WIDTH_IN_DP*2, roundBitmap.getConfig());
        Canvas canvas1 = new Canvas(bmpWithBorder);
        canvas1.drawColor(Color.WHITE);
        canvas1.drawBitmap(roundBitmap, DrawerRoundedImageView.HARDCODED_BORDER_WIDTH_IN_DP, DrawerRoundedImageView.HARDCODED_BORDER_WIDTH_IN_DP, null);

        roundBitmap = getRoundedCornerBitmap(bmpWithBorder, w);

        canvas.drawBitmap(roundBitmap, 0, 0, null);

    }

    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {

        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

}
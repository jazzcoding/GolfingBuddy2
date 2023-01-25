package com.golfingbuddy.ui.auth;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.golfingbuddy.R;
import com.golfingbuddy.core.SkBaseActivity;

/**
 * Created by sardar on 6/12/14.
 */
public class BaseAuthActivity extends SkBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        onCreate(savedInstanceState, true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState, boolean getSiteData) {
        super.onCreate(savedInstanceState, getSiteData);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.auth_auth_bg);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = bmp.getWidth(), height = bmp.getHeight();

        if( dm.widthPixels > width ){
            double scale = ((double)dm.widthPixels)/((double)bmp.getWidth());
            width = dm.widthPixels;
            height = (int)Math.round(bmp.getHeight()*scale);
        }

        if( dm.heightPixels > height ){
            double scale = ((double)dm.heightPixels)/((double)bmp.getHeight());
            width = (int)Math.round(bmp.getWidth()*scale);
            height = dm.heightPixels;
        }

        bmp = Bitmap.createScaledBitmap(bmp, width, height, false);
        ImageView image = (ImageView)findViewById(R.id.imageView_UrlBackground);
        image.setImageBitmap(bmp);
    }
}

package com.golfingbuddy.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import android.util.Log;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

/**
 * Created by jk on 10/28/14.
 */
public class SKImageView extends ImageView {

    protected Drawable placeholderDrawable = null;
    protected int placeholderResId = 0;

    protected int targetWidth = 0;
    protected int targetHeight = 0;

    public SKImageView(Context context)
    {
        super(context);
    }

    public SKImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public SKImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context,attrs,defStyle);
    }

    public SKImageView placeholder(int placeholderResId) {
        if (placeholderResId == 0) {
            throw new IllegalArgumentException("Placeholder image resource invalid.");
        }
        if (placeholderDrawable != null) {
            throw new IllegalStateException("Placeholder image already set.");
        }
        this.placeholderResId = placeholderResId;
        return this;
    }

    /**
     * A placeholder drawable to be used while the image is being loaded. If the requested image is
     * not immediately available in the memory cache then this resource will be set on the target
     * {@link ImageView}.
     * <p>
     * If you are not using a placeholder image but want to clear an existing image (such as when
     * used in an {@link android.widget.Adapter adapter}), pass in {@code null}.
     */
    public SKImageView placeholder(Drawable placeholderDrawable) {
        if (placeholderResId != 0) {
            throw new IllegalStateException("Placeholder image already set.");
        }
        this.placeholderDrawable = placeholderDrawable;
        return this;
    }

    public SKImageView setSize(int width, int height) {
        if (width > 0) {
            targetWidth = width;
        }

        if (height > 0) {
            targetHeight = height;
        }

        return this;
    }

    private RequestCreator getLoader(String url) {
        RequestCreator loader = Picasso.with(this.getContext()).load(url).noFade();

        if ( this.placeholderDrawable != null )
        {
            loader.placeholder(this.placeholderDrawable);
        }
        else if ( placeholderResId != 0 )
        {
            loader.placeholder(this.placeholderResId);
        }

        if ( targetWidth > 0 && targetHeight > 0 )
        {
            loader.resize(targetWidth, targetHeight);
        }

        return loader;
    }

    public void setImageUrl(String url)
    {
        final SKImageView view = this;
        try {
            RequestCreator loader = getLoader(url);
            loader.into(this);
        }
        catch (IllegalArgumentException ex)
        {
            Log.e("SKImageView", ex.getMessage());
        }

    }

    public void setImageUrl(String url, Callback callback) {
        try {
            RequestCreator loader = getLoader(url);
            loader.into(this, callback);
        }
        catch (IllegalArgumentException ex)
        {
            Log.e("SKImageView", ex.getMessage());
        }
    }
}

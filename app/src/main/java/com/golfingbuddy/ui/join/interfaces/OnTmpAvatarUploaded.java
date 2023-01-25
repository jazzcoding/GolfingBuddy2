package com.golfingbuddy.ui.join.interfaces;

/**
 * Created by jk on 2/9/16.
 */
public interface OnTmpAvatarUploaded {

    public void onSuccess( String message );
    public void onFailed( String message );
    public void onError(Exception ex);

}

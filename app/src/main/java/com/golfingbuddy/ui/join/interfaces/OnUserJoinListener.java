package com.golfingbuddy.ui.join.interfaces;

import java.util.HashMap;

/**
 * Created by jk on 1/28/16.
 */
public interface OnUserJoinListener {
    public void onSaveSuccess(HashMap<String, String> data);
    public void onSaveFaild(HashMap<String, String> data);
}

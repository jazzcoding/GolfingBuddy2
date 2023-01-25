package com.golfingbuddy.ui.join.interfaces;

/**
 * Created by jk on 1/20/16.
 */
public interface OnLoadedListener {
    public void onSuccess(QuestionsModel model);
    public void onError(QuestionsModel model, Exception ex);
}


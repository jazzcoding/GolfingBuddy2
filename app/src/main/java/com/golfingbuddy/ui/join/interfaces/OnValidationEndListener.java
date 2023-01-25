package com.golfingbuddy.ui.join.interfaces;


import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.HashMap;

/**
 * Created by jk on 2/1/16.
 */
public interface OnValidationEndListener {
    public void onValidationEnd(HashMap<String, QuestionService.QuestionValidationInfo> validationInfo);
    public void onError(Exception ex);
}


package com.golfingbuddy.ui.base.email_verification;

import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by jk on 2/18/16.
 */
public interface Presenter {
    public LinkedList<QuestionService.QuestionParams> getQuestions();
    public void onDoneClicked(HashMap<String, String> data);
    public void onResendClicked(HashMap<String, String> data);
    public void validateField(HashMap<String, String> data);
}

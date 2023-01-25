package com.golfingbuddy.ui.base.email_verification;

import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jk on 2/18/16.
 */
public interface MainView {
    public void clean();
    public void drawQuestions(LinkedList<QuestionService.QuestionParams> list);
    public void drawErrors(Collection<QuestionService.QuestionValidationInfo> errors);
    public void removeErrors(List<QuestionService.QuestionParams> questions);
    public HashMap<String, String> getData();
    public void close();
    public void showMessage(String text);
}

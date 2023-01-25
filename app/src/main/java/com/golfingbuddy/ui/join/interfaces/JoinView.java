package com.golfingbuddy.ui.join.interfaces;

import com.golfingbuddy.ui.search.classes.QuestionObject;
import com.golfingbuddy.ui.search.classes.SectionObject;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.Collection;
import java.util.List;

/**
 * Created by jk on 1/21/16.
 */
public interface JoinView {

    public void clean();

    public void showProgress();

    public void hideProgress();

    public void drawQuestions(SectionObject[] sections, QuestionObject[] questions);

    public void drawErrors(Collection<QuestionService.QuestionValidationInfo> errors);

    public void removeErrors(List<QuestionObject> questions);

    public void changeNextButtonName(int resorce);

    public void close();

    public void showMessage(String text);

    public void login(String username, String password);

    public void showError(String text);
}

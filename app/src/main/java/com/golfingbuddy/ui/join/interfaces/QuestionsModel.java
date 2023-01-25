package com.golfingbuddy.ui.join.interfaces;

import com.golfingbuddy.ui.search.classes.QuestionObject;
import com.golfingbuddy.ui.search.classes.SectionObject;

import java.util.HashMap;

/**
 * Created by jk on 1/20/16.
 */
public interface QuestionsModel {

    public void loadData(int step);

    public void loadData(int step, int gender);

    public boolean isLoadInProcess();

    public QuestionObject[] getQuestions();

    public SectionObject[] getSections();

    public QuestionObject[] getQuestions(HashMap<String, String> params);

    public String isAvatarRequired();
    
    public String isTermsOfUserRequired();
}

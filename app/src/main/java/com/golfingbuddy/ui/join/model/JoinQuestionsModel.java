package com.golfingbuddy.ui.join.model;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.model.base.BaseRestCommand;
import com.golfingbuddy.model.base.BaseServiceHelper;
import com.golfingbuddy.ui.join.interfaces.OnLoadedListener;
import com.golfingbuddy.ui.join.interfaces.QuestionsModel;
import com.golfingbuddy.ui.search.classes.QuestionObject;
import com.golfingbuddy.ui.search.classes.SectionObject;
import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by jk on 1/20/16.
 */
public class JoinQuestionsModel implements QuestionsModel  {
    private int requestId = -1;
    private OnLoadedListener listner;
    private BaseServiceHelper helper;
    private QuestionData data;

    public JoinQuestionsModel(BaseServiceHelper helper, OnLoadedListener listner)
    {
        this.listner = listner;
        this.helper = helper;
        this.data = new QuestionData();
    }

    @Override
    public void loadData(int step) {
        this.loadData(step, 0);
    }

    @Override
    public void loadData(final int step, int gender) {
        if ( requestId > -1 )
        {
            return;
        }

        HashMap<String,String> params = new HashMap<String,String>();
        params.put("step", String.valueOf(step));

        if ( gender > 0 )
        {
            params.put("sex", String.valueOf(gender));
        }

        requestId = helper.runRestRequest(BaseRestCommand.ACTION_TYPE.GET_SIGN_UP_QUESTIONS, params,
            new SkServiceCallbackListener() {
                @Override
                public void onServiceCallback(int rId, Intent requestIntent, int resultCode, Bundle bundle) {
                    JoinQuestionsModel.this.data = new QuestionData();

                    try {
                        String result = bundle.getString("data");

                        if (result != null) {
                            JsonObject dataObject = new Gson().fromJson(result, JsonObject.class);
                            JsonElement listData = dataObject.get("data");
                            JsonElement type = dataObject.get("type");

                            if (listData != null && listData.isJsonObject() && type != null && "success".equals(type.getAsString())) {
                                JsonObject data = listData.getAsJsonObject();

                                LinkedList<QuestionObject> questionList = new LinkedList<QuestionObject>();

                                Gson g = new GsonBuilder().create();
                                JoinQuestionsModel.this.data = g.fromJson(data, QuestionData.class);

                                if( JoinQuestionsModel.this.data.getQuestions() != null && JoinQuestionsModel.this.data.getQuestions().length > 0 ) {

                                    if (step == UserData.STEP_1 && JoinQuestionsModel.this.data.isDisplayAvatar()) {
                                        QuestionObject avatar = new QuestionObject();
                                        avatar.setName("avatar");
                                        avatar.setRequired(JoinQuestionsModel.this.data.isAvatarRequired());
                                        avatar.setPresentation(QuestionService.getInstance().QUESTION_PRESENTATION_AVATAR);
                                        questionList.add(avatar);

                                        JoinQuestionsModel.this.data.setSections(null);
                                    }

                                    questionList.addAll(Arrays.asList(JoinQuestionsModel.this.data.getQuestions()));

                                    if (step == UserData.STEP_2 && ("true".equals(JoinQuestionsModel.this.data.isTermsOfUseRequired()) || "1".equals(JoinQuestionsModel.this.data.isTermsOfUseRequired()))) {
                                        QuestionObject terms = new QuestionObject();
                                        terms.setName("terms");
                                        terms.setLabel("");
                                        terms.setRequired("1");
                                        terms.setPresentation(QuestionService.getInstance().QUESTION_PRESENTATION_TERMS_OF_USE);
                                        questionList.add(terms);
                                    }

                                    QuestionObject[] questions = new QuestionObject[questionList.size()];
                                    JoinQuestionsModel.this.data.setQuestions(questionList.toArray(questions));
                                }

                                JoinQuestionsModel.this.listner.onSuccess(JoinQuestionsModel.this);
                            }
                        }
                    } catch (Exception ex) {
                        Log.i(" build join form  ", ex.getMessage(), ex);
                        JoinQuestionsModel.this.listner.onError(JoinQuestionsModel.this, ex);
                    } finally {
                        JoinQuestionsModel.this.requestId = -1;
                    }
                }
            });
    }

    @Override
    public boolean isLoadInProcess() {
        return requestId != -1;
    }

    @Override
    public QuestionObject[] getQuestions() {
        return this.data.getQuestions();
    }

    @Override
    public SectionObject[] getSections() {
        return this.data.getSections();
    }

    public String isAvatarRequired() {
        return this.data.isAvatarRequired();
    }
    public String isTermsOfUserRequired() {
        return this.data.isTermsOfUseRequired();
    }

    @Override
    public QuestionObject[] getQuestions(HashMap<String, String> params) {
        return null;
    }

    public static class QuestionData {
        private QuestionObject[] questions = null;
        private SectionObject[] sections = null;
        private String avatarRequired = null;
        private String termsOfUseRequired = null;

        public boolean isDisplayAvatar() {
            return "true".equals(displayAvatar) || "1".equals(displayAvatar);
        }

        public void setDisplayAvatar(String displayAvatar) {
            this.displayAvatar = displayAvatar;
        }

        private String displayAvatar = null;

        public String isAvatarRequired() {
            return avatarRequired;
        }

        public void setAvatarRequired(String avatarRequired) {
            this.avatarRequired = avatarRequired;
        }

        public String isTermsOfUseRequired() {
            return termsOfUseRequired;
        }

        public void setTermsOfUseRequired(String termsOfUserRequired) {
            this.termsOfUseRequired = termsOfUserRequired;
        }

        public QuestionObject[] getQuestions() {
            return questions;
        }

        public void setQuestions(QuestionObject[] questions) {
            this.questions = questions;
        }

        public SectionObject[] getSections() {
            return sections;
        }

        public void setSections(SectionObject[] sections) {
            this.sections = sections;
        }
    }
}

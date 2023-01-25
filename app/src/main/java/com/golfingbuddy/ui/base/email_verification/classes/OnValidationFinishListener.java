package com.golfingbuddy.ui.base.email_verification.classes;

import com.golfingbuddy.ui.search.service.QuestionService;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Created by jk on 2/19/16.
 */
public interface OnValidationFinishListener {
    public void onRemoveErrors(LinkedList<QuestionService.QuestionParams> list);
    public void onDrawErrors(Collection<QuestionService.QuestionValidationInfo> errors, int type);
}

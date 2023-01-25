package com.golfingbuddy.ui.matches.service;

import android.app.Application;
import android.content.Intent;

import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.core.SkServiceHelper;
import com.golfingbuddy.ui.matches.classes.MatchesRestRequestCommand;

/**
 * Created by kairat on 12/1/14.
 */
public class MatchesListService extends SkServiceHelper {
    /**
     * Constructor
     *
     * @param app
     */
    public MatchesListService(Application app) {
        super(app);
    }

    public int getMatchesList(SkServiceCallbackListener listener) {
        final int requestId = createId();
        Intent i = createIntent(application, new MatchesRestRequestCommand("newest"), requestId);
        return runRequest(requestId, i, listener);
    }

    public int getMatchesList(String sort, SkServiceCallbackListener listener) {
        final int requestId = createId();
        Intent i = createIntent(application, new MatchesRestRequestCommand(sort), requestId);
        return runRequest(requestId, i, listener);
    }

    public int getMatchesList(String sort, int page, SkServiceCallbackListener listener) {
        final int requestId = createId();
        Intent i = createIntent(application, new MatchesRestRequestCommand(sort, page), requestId);
        return runRequest(requestId, i, listener);
    }
}

package com.golfingbuddy.ui.fbconnect.service;

import android.app.Application;
import android.content.Intent;

import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.core.SkServiceHelper;
import com.golfingbuddy.ui.fbconnect.classes.FacebookConnectRestRequestCommand;

import java.util.HashMap;

/**
 * Created by kairat on 12/8/14.
 */
public class FacebookConnectService extends SkServiceHelper {
    public static final int CRITICALC_COUNT = 5;
    public static final int COUNT = 20;

    /**
     * Constructor
     *
     * @param app
     */
    public FacebookConnectService(Application app) {
        super(app);
    }

    public int tryLogin(String facebookId, SkServiceCallbackListener listener) {
        final int requestId = createId();
        Intent i = createIntent(application, new FacebookConnectRestRequestCommand(facebookId), requestId);
        return runRequest(requestId, i, listener);
    }

    public int getFirstStepQuestionList(SkServiceCallbackListener listener) {
        final int requestId = createId();
        Intent i = createIntent(application, new FacebookConnectRestRequestCommand(1), requestId);
        return runRequest(requestId, i, listener);
    }

    public int getSecondStepQuestionList(String sex, String email, SkServiceCallbackListener listener) {
        final int requestId = createId();
        Intent i = createIntent(application, new FacebookConnectRestRequestCommand(2, sex, email), requestId);
        return runRequest(requestId, i, listener);
    }

    public int save(HashMap<String, String> data, SkServiceCallbackListener listener) {
        final int requestId = createId();
        Intent i = createIntent(application, new FacebookConnectRestRequestCommand(data), requestId);
        return runRequest(requestId, i, listener);
    }
}

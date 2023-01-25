package com.golfingbuddy.model.auth;

import android.app.Application;
import android.content.Intent;

import com.golfingbuddy.core.SkServiceHelper;

/**
 * Created by sardar on 6/2/14.
 */
public class AuthServiceHelper extends SkServiceHelper {
    public AuthServiceHelper(Application app) {
        super(app);
    }

    public int getSiteInfo(){
        final int requestId = createId();
//        SkRestRequestCommandCallback callback = new SkRestRequestCommandCallback() {
//            @Override
//            public JsonArray call(SkRestInterface skRest) {
//                return skRest.siteInfo();
//            }
//        };

//        Intent i = createIntent(application, new RestRequestCommand(), requestId);
//        return runRequest(requestId, i);

        return 1;

    }

    public int authenticate( final String username, final String password )
    {
        final int requestId = createId();
        Intent i = createIntent(application, new LoginCommand(username, password), requestId);
        return runRequest(requestId, i);
    }


}

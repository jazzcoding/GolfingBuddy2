package com.golfingbuddy.ui.speedmatch.service;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.golfingbuddy.core.SkServiceCallbackListener;
import com.golfingbuddy.core.SkServiceHelper;
import com.golfingbuddy.ui.speedmatch.classes.SpeedmatchesFilter;
import com.golfingbuddy.ui.speedmatch.classes.SpeedmatchesRestRequestCommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by kairat on 12/8/14.
 */
public class SpeedmatchesService extends SkServiceHelper {
    public static final int CRITICALC_COUNT = 5;
    public static final int COUNT = 20;
    private int mCommnad;

    /**
     * Constructor
     *
     * @param app
     */
    public SpeedmatchesService(Application app) {
        super(app);
    }

    public SpeedmatchesFilter getFilterSettings(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SpeedmatchesFilter filter = new SpeedmatchesFilter();

        filter.setMatchAge(prefs.getString("speedmatches_age", ""));
        filter.setDefaultAgeRange(prefs.getString("speedmatches_age_range", ""));

        HashMap<String, Object> googleSettings = new HashMap<>();
        googleSettings.put("distance", prefs.getString("speedmatches_distance", ""));
        filter.setGooglemapLocation(googleSettings);

        Set<String> showMeList = prefs.getStringSet("speedmatches_show_me", null);

        if (showMeList != null) {
            filter.setMatchSex(new ArrayList<>(showMeList));
        }

        return filter;
    }

    public SpeedmatchesFilter mergeSettings(SpeedmatchesFilter oldFilter, SpeedmatchesFilter newFilter) {
        oldFilter.getGooglemapLocation().put("distance", newFilter.getGooglemapLocation().get("distance"));
        oldFilter.setMatchAge(newFilter.getMatchAge());

        if (newFilter.getMatchSex() != null) {
            oldFilter.setMatchSex(newFilter.getMatchSex());
        }

        return oldFilter;
    }

    public void cancel() {
        cancel(mCommnad);
    }

    public void cancel(int command) {
        if (isPending(command)) {
            cancelCommand(command);
        }
    }

    public int getMatchesList( SkServiceCallbackListener listener, SpeedmatchesFilter filterSettings )
    {
        cancel(mCommnad);
        final int requestId = createId();
        Intent i = createIntent(application, new SpeedmatchesRestRequestCommand(filterSettings), requestId);
        mCommnad = runRequest(requestId, i, listener);

        return mCommnad;
    }

    public int getMatchesList( ArrayList<String> excludeList, SkServiceCallbackListener listener )
    {
        cancel(mCommnad);
        final int requestId = createId();
        Intent i = createIntent(application, new SpeedmatchesRestRequestCommand(excludeList), requestId);
        mCommnad = runRequest(requestId, i, listener);

        return mCommnad;
    }

    public int likeUser( int userId, SkServiceCallbackListener listener ) {
        final int requestId = createId();
        Intent i = createIntent(application, new SpeedmatchesRestRequestCommand(userId, SpeedmatchesRestRequestCommand.Command.LIKE_USER), requestId);
        return runRequest(requestId, i, listener);
    }

    public int skipUser( int userId, SkServiceCallbackListener listener ) {
        final int requestId = createId();
        Intent i = createIntent(application, new SpeedmatchesRestRequestCommand(userId, SpeedmatchesRestRequestCommand.Command.SKIP_USER), requestId);
        return runRequest(requestId, i, listener);
    }
}

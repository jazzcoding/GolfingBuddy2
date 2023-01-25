package com.golfingbuddy.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.util.SparseArray;

import org.apache.commons.logging.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Created by sardar on 5/7/14.
 */
public class SkServiceHelper {
    /**
     * Common listeners to be notified every request
     */
    private ArrayList<SkServiceCallbackListener> commonListeners = new ArrayList<SkServiceCallbackListener>();

    /**
     * Special listeners who have assigned request Id
     */
    private HashMap<Integer, ArrayList<SkServiceCallbackListener>> specListiners = new HashMap<Integer, ArrayList<SkServiceCallbackListener>>();

    /**
     * Request id counter
     */
    private AtomicInteger idCounter = new AtomicInteger();

    /**
     * Intent list being processed
     */
    private SparseArray<Intent> pendingActivities = new SparseArray<Intent>();

    /**
     * Application context
     */
    protected Application application;

    /**
     * Constructor
     *
     * @param app
     */
    public SkServiceHelper(Application app) {
        this.application = app;
    }

    /**
     * Adds listeners to general list
     *
     * @param listener
     */
    public void addListener(SkServiceCallbackListener listener) {
        commonListeners.add(listener);
    }

    /**
     * Removes listeners from general list
     *
     * @param listener
     */
    public void removeListener(SkServiceCallbackListener listener) {
        commonListeners.remove(listener);
    }

    /**
     * Adds listeners to spec list
     *
     * @param listener
     * @param requestId
     */
    public void addListener(SkServiceCallbackListener listener, int requestId) {
        ArrayList<SkServiceCallbackListener> list = specListiners.get(requestId);

        if (list == null) {
            list = new ArrayList<SkServiceCallbackListener>();
            specListiners.put(requestId, list);
        }

        list.add(listener);
    }

    /**
     * Removes listeners from spec list
     *
     * @param currentListener
     * @param requestId
     */
    public void removeListener(SkServiceCallbackListener currentListener, int requestId) {
        ArrayList<SkServiceCallbackListener> list = specListiners.get(requestId);

        if (list != null) {
            list.remove(currentListener);
        }
    }

    /**
     * Cancels command
     *
     * @param requestId
     */
    public void cancelCommand(int requestId) {
        Intent i = new Intent(application, SkCommandExecutorService.class);
        i.setAction(SkCommandExecutorService.ACTION_CANCEL_COMMAND);
        i.putExtra(SkCommandExecutorService.EXTRA_REQUEST_ID, requestId);

        application.startService(i);
        pendingActivities.remove(requestId);
        specListiners.remove(requestId);
    }

    public boolean isPending(int requestId) {
        return pendingActivities.get(requestId) != null;
    }

    public boolean check(Intent intent, Class<? extends SkBaseCommand> clazz) {
        Parcelable commandExtra = intent.getParcelableExtra(SkCommandExecutorService.EXTRA_COMMAND);
        return commandExtra != null && commandExtra.getClass().equals(clazz);
    }

    protected int createId() {
        return idCounter.getAndIncrement();
    }

    protected int runRequest(final int requestId, Intent i) {
        pendingActivities.append(requestId, i);
        application.startService(i);
        return requestId;
    }

    protected int runRequest(final int requestId, Intent i, SkServiceCallbackListener listener) {
        addListener(listener, requestId);
        return runRequest(requestId,i);
    }

    protected Intent createIntent(final Context context, SkBaseCommand command, final int requestId) {
        Intent i = new Intent(context, SkCommandExecutorService.class);
        i.setAction(SkCommandExecutorService.ACTION_EXECUTE_COMMAND);
        i.putExtra(SkCommandExecutorService.EXTRA_COMMAND, command);
        i.putExtra(SkCommandExecutorService.EXTRA_REQUEST_ID, requestId);
        i.putExtra(SkCommandExecutorService.EXTRA_STATUS_RECEIVER, new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                Intent originalIntent = pendingActivities.get(requestId);
                if (isPending(requestId)) {
                    if (resultCode != SkBaseCommand.RESPONSE_PROGRESS) {
                        pendingActivities.remove(requestId);
                    }

                    try {
                        for (SkServiceCallbackListener currentListener : commonListeners) {
                            if (currentListener != null) {
                                currentListener.onServiceCallback(requestId, originalIntent, resultCode, resultData);
                            }
                        }

                        for (Map.Entry<Integer, ArrayList<SkServiceCallbackListener>> currentEntry : specListiners.entrySet()) {
                            if (currentEntry.getKey() == requestId) {
                                for (SkServiceCallbackListener currentListener : currentEntry.getValue()) {
                                    if (currentListener != null) {
                                        currentListener.onServiceCallback(requestId, originalIntent, resultCode, resultData);
                                    }
                                }
                            }
                        }
                    }
                    catch( Exception e ){
                        android.util.Log.d("tag", e.toString());
                    }
                }
            }
        });

        return i;
    }
}

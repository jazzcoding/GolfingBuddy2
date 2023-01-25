package com.golfingbuddy.core;

import android.app.Service;
import android.content.Intent;
import android.os.*;
import android.os.Process;
import android.text.TextUtils;
import android.util.SparseArray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by sardar on 5/30/14.
 */
public class SkCommandExecutorService extends Service {

    private static final int NUM_THREADS = 1;
    public static final String ACTION_EXECUTE_COMMAND = SkApplication.PACKAGE.concat(".ACTION_EXECUTE_COMMAND");
    public static final String ACTION_CANCEL_COMMAND = SkApplication.PACKAGE.concat(".ACTION_CANCEL_COMMAND");
    public static final String EXTRA_REQUEST_ID = SkApplication.PACKAGE.concat(".EXTRA_REQUEST_ID");
    public static final String EXTRA_STATUS_RECEIVER = SkApplication.PACKAGE.concat(".STATUS_RECEIVER");
    public static final String EXTRA_COMMAND = SkApplication.PACKAGE.concat(".EXTRA_COMMAND");

    private ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

    private SparseArray<RunningCommand> runningCommands = new SparseArray<RunningCommand>();

    protected void onHandleIntent(Intent intent) {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            getCommand(intent).execute(intent, getApplicationContext(), getReceiver(intent));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        executor.shutdownNow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (ACTION_EXECUTE_COMMAND.equals(intent.getAction())) {
            RunningCommand runningCommand = new RunningCommand(intent);

            synchronized (runningCommands) {
                runningCommands.append(getCommandId(intent), runningCommand);
            }

            executor.submit(runningCommand);
        }
        if (ACTION_CANCEL_COMMAND.equals(intent.getAction())) {
            RunningCommand runningCommand = runningCommands.get(getCommandId(intent));
            if (runningCommand != null) {
                runningCommand.cancel();
            }
        }

        return START_NOT_STICKY;
    }

    private class RunningCommand implements Runnable {

        private Intent intent;

        private SkBaseCommand command;

        public RunningCommand(Intent intent) {
            this.intent = intent;

            command = getCommand(intent);
        }

        public void cancel() {
            command.cancel();
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            command.execute(intent, getApplicationContext(), getReceiver(intent));

            shutdown();
        }

        private void shutdown() {
            synchronized (runningCommands) {
                runningCommands.remove(getCommandId(intent));
                if (runningCommands.size() == 0) {
                    stopSelf();
                }
            }
        }

    }

    private ResultReceiver getReceiver(Intent intent) {
        return intent.getParcelableExtra(EXTRA_STATUS_RECEIVER);
    }

    private SkBaseCommand getCommand(Intent intent) {
        return intent.getParcelableExtra(EXTRA_COMMAND);
    }

    private int getCommandId(Intent intent) {
        return intent.getIntExtra(EXTRA_REQUEST_ID, -1);
    }

}


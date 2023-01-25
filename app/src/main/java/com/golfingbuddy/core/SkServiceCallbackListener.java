package com.golfingbuddy.core;

/**
 * Created by sardar on 5/30/14.
 */
import android.content.Intent;
import android.os.Bundle;

public interface SkServiceCallbackListener {

    void onServiceCallback(int requestId, Intent requestIntent, int resultCode, Bundle data);

}
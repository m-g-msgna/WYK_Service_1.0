package org.ntnu.wyk_service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent){
        Intent serviceIntent = new Intent(context, WYKService.class);
        context.startService(serviceIntent);
    }
}

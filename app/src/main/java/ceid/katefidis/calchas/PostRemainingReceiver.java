package ceid.katefidis.calchas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PostRemainingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PostRemainingUtil.scheduleJob(context);
    }
}
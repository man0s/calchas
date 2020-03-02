package ceid.katefidis.calchas;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class OutCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences preferences = context.getSharedPreferences("MyPrefs", 0);//PreferenceManager.getDefaultSharedPreferences(context);
        Editor e = preferences.edit();
        int calls = preferences.getInt("total_calls", 0);
        calls++;
        e.putInt("total_calls", calls);
        e.commit();
        Log.i("Calchas Receiver", "Total calls: " + preferences.getInt("total_calls", 0));
    }

}

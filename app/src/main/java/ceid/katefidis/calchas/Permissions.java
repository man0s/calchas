package ceid.katefidis.calchas;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class Permissions {

    //Request Permissions
    public static void Request_PERMISSIONS(Activity act, int code)
    {
        ActivityCompat.requestPermissions(act, new
                String[]{Manifest.permission.READ_CALL_LOG, Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.PROCESS_OUTGOING_CALLS, Manifest.permission.READ_SMS, Manifest.permission.WRITE_CALL_LOG },code);
    }

    //Check Permissions
    public static boolean Check_PERMISSIONS(Activity act) {
        boolean granted = false;
        if (ContextCompat.checkSelfPermission(act, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(act, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(act, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(act, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(act, Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(act, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(act, Manifest.permission.WRITE_CALL_LOG) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(act, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_GRANTED) {
            granted = true;
        }
        return granted;
    }
}
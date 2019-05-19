package ceid.katefidis.calchas;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

public class NotificationListener extends NotificationListenerService {
    /*
        These are the package names of the apps. for which we want to
        listen the notifications
     */
    private static final class ApplicationPackageNames {
//        public static final String FACEBOOK_PACK_NAME = "com.facebook.katana";
//        public static final String FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca";
//        public static final String INSTAGRAM_PACK_NAME = "com.instagram.android";
        public static final String WHATSAPP_PACK_NAME = "com.whatsapp";
        public static final String VIBER_PACK_NAME = "com.viber.voip";
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    public static final class InterceptedNotificationCode {
//        public static final int FACEBOOK_CODE = 1;
//        public static final int INSTAGRAM_CODE = 2;
//        public static final int MESSENGER_CODE = 3;
        public static final int WHATSAPP_CODE = 1;
        public static final int VIBER_CODE = 2;
        public static final int OTHER_NOTIFICATIONS_CODE = 3; // We ignore all notification with code == 4
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    private String PreviousNotificationKey;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);

        if (notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {
            if(!sbn.getKey().equals(PreviousNotificationKey)) {
                Intent intent = new Intent("ceid.katefidis.calchas");
                Bundle extras = new Bundle();
                Log.i("Social", sbn.getKey());
                extras.putInt("Notification Code", notificationCode);
                extras.putLong("Post Time", sbn.getPostTime());
                extras.putString("Contact", sbn.getNotification().extras.getString("android.title"));
                intent.putExtras(extras);
                sendBroadcast(intent);
            }
        }

        //Viber Duplicate Notification Bug Fix
        //metavliti gia na krataei to key tou prohgoumenou notification
        //ongoing notification key gia call/videocall einai |201|null|
        if(sbn.getKey().contains("|201|null|"))  PreviousNotificationKey = sbn.getKey();
        else PreviousNotificationKey = null;
    }

//    @Override
//    public void onNotificationRemoved(StatusBarNotification sbn){
//        int notificationCode = matchNotificationCode(sbn);
//
//        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {
//
//            StatusBarNotification[] activeNotifications = this.getActiveNotifications();
//
//            if(activeNotifications != null && activeNotifications.length > 0) {
//                for (int i = 0; i < activeNotifications.length; i++) {
//                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
//                        Intent intent = new  Intent("ceid.katefidis.calchas");
//                        Bundle extras = new Bundle();
//                        extras.putInt("Notification Code", notificationCode);
//                        extras.putLong("Post Time", activeNotifications[i].getPostTime());
//                        extras.putString("Contact", sbn.getNotification().extras.getString("android.title"));
//                        intent.putExtras(extras);
//                        sendBroadcast(intent);
//                        break;
//                    }
//                }
//            }
//        }
//    }

    private int matchNotificationCode(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

//        if(packageName.equals(ApplicationPackageNames.FACEBOOK_PACK_NAME)
//                || packageName.equals(ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME)){
//            return(InterceptedNotificationCode.FACEBOOK_CODE);
//        }
//        else if(packageName.equals(ApplicationPackageNames.INSTAGRAM_PACK_NAME)){
//            return(InterceptedNotificationCode.INSTAGRAM_CODE);
//        }
        if(packageName.equals(ApplicationPackageNames.WHATSAPP_PACK_NAME)){
            if(sbn.getKey().contains("|1|null|") || sbn.getKey().contains("|4|null|")) //text msg || call bug fix (case "|2131297581|null|" probably is unstable)
                return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
             else {
                 if(sbn.getKey().contains("|1|") || sbn.getKey().contains("|7|null|") ) //|1|etc| for msg - |7|null for call/video call
                    return(InterceptedNotificationCode.WHATSAPP_CODE);
                 else return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
            }
        }
        else if(packageName.equals(ApplicationPackageNames.VIBER_PACK_NAME)){
            if(sbn.getKey().contains("|-100|message|") || sbn.getKey().contains("|201|null|")) //text msg || call/vid call
                return(InterceptedNotificationCode.VIBER_CODE);
            else
                return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
        else{
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }
}

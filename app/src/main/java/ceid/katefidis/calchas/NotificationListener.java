package ceid.katefidis.calchas;

import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

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

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);

        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE){
            Intent intent = new  Intent("ceid.katefidis.calchas");
            Bundle extras = new Bundle();
            extras.putInt("Notification Code", notificationCode);
            extras.putLong("Post Time", sbn.getPostTime());
            extras.putString("Contact", sbn.getNotification().extras.getString("android.title"));
            intent.putExtras(extras);
            sendBroadcast(intent);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        int notificationCode = matchNotificationCode(sbn);

        if(notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {

            StatusBarNotification[] activeNotifications = this.getActiveNotifications();

            if(activeNotifications != null && activeNotifications.length > 0) {
                for (int i = 0; i < activeNotifications.length; i++) {
                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                        Intent intent = new  Intent("ceid.katefidis.calchas");
                        Bundle extras = new Bundle();
                        extras.putInt("Notification Code", notificationCode);
                        extras.putLong("Post Time", activeNotifications[i].getPostTime());
                        extras.putString("Contact", sbn.getNotification().extras.getString("android.title"));
                        intent.putExtras(extras);
                        sendBroadcast(intent);
                        break;
                    }
                }
            }
        }
    }

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
            return(InterceptedNotificationCode.WHATSAPP_CODE);
        }
        else if(packageName.equals(ApplicationPackageNames.VIBER_PACK_NAME)){
            return(InterceptedNotificationCode.VIBER_CODE);
        }
        else{
            return(InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE);
        }
    }
}

package com.metinkale.prayer.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;

import com.metinkale.prayer.base.R;

import java.util.Set;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.collection.ArraySet;
import androidx.core.app.NotificationCompat;

/**
 * this class holds a foreground service as long there are any needies.
 * <p>
 * If there are no needies (ongoing notifications or widgets), this service will not stay awake
 */
public class ForegroundService extends Service {
    
    private static final String ACTION_ADD_NEEDY = "addNeedy";
    private static final String ACTION_REMOVE_NEEDY = "removeNeedy";
    private static final String EXTRA_NEEDY = "needy";
    private static final String EXTRA_NOTIFICATION = "notification";
    private static final String EXTRA_NOTIFICATION_ID = "notificationId";
    private Set<String> mForegroundNeedy = new ArraySet<>();
    private Notification mNotification;
    private int mNotificationId;
    private String mNotificationNeedy;
    
    
    public static void addNeedy(Context c, String needy, Notification not, int notId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(c, ForegroundService.class);
            intent.setAction(ACTION_ADD_NEEDY);
            intent.putExtra(EXTRA_NEEDY, needy);
            intent.putExtra(EXTRA_NOTIFICATION, not);
            intent.putExtra(EXTRA_NOTIFICATION_ID, notId);
            c.startForegroundService(intent);
        }
    }
    
    public static void addNeedy(Context c, String needy) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(c, ForegroundService.class);
            intent.setAction(ACTION_ADD_NEEDY);
            intent.putExtra(EXTRA_NEEDY, needy);
            c.startForegroundService(intent);
        }
    }
    
    
    public static void removeNeedy(Context c, String needy) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(c, ForegroundService.class);
            intent.setAction(ACTION_REMOVE_NEEDY);
            intent.putExtra(EXTRA_NEEDY, needy);
            c.startForegroundService(intent);
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            
            String action;
            if (intent != null && (action = intent.getAction()) != null) {
                switch (action) {
                    case ACTION_ADD_NEEDY: {
                        String needy = intent.getStringExtra(EXTRA_NEEDY);
                        mForegroundNeedy.add(needy);
                        if (intent.hasExtra(EXTRA_NOTIFICATION)) {
                            mNotification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
                            mNotificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
                            mNotificationNeedy = needy;
                            
                            if (mNotification != null) {
                                //stopForeground(true);
                                startForeground(mNotificationId, mNotification);
                            }
                        }
                        break;
                    }
                    case ACTION_REMOVE_NEEDY: {
                        String needy = intent.getStringExtra(EXTRA_NEEDY);
                        mForegroundNeedy.remove(needy);
                        if (needy.equals(mNotificationNeedy)) {
                            mNotification = null;
                            mNotificationNeedy = null;
                            mNotificationId = 0;
                            
                            if (!mForegroundNeedy.isEmpty()) {
                                //stopForeground(true);
                                startForeground(getNotificationId(), createNotification());
                            }
                        }
                        break;
                    }
                }
            }
        }
        
        
        if (mForegroundNeedy.isEmpty()) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        
        
        return START_STICKY;
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(getNotificationId(), createNotification());
        }
    }
    
    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
        super.onDestroy();
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected Notification createNotification() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        
        String channelId = "foreground";
        if (nm.getNotificationChannel(channelId) == null) {
            nm.createNotificationChannel(new NotificationChannel(channelId, getString(R.string.appName), NotificationManager.IMPORTANCE_MIN));
        }
        
        Intent intent = new Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName())
                .putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setContentTitle(getString(R.string.appName));
        builder.setContentIntent(pendingIntent);
        return builder.build();
    }
    
    
    private int getNotificationId() {
        return 571;
    }
}

package android.example.com.squawker.fcm;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

// TODO (1) Make a new Service in the fcm package that extends from FirebaseMessagingService.

public class MyFirebaseMessagingService extends FirebaseMessagingService {


    private static final String JSON_KEY_AUTHOR = SquawkContract.COLUMN_AUTHOR;
    private static final String JSON_KEY_AUTHOR_KEY = SquawkContract.COLUMN_AUTHOR_KEY;
    private static final String JSON_KEY_MESSAGE = SquawkContract.COLUMN_MESSAGE;
    private static final String JSON_KEY_DATE = SquawkContract.COLUMN_DATE;
    public static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    private static final int NOTIFICATION_MAX_CHARACTERS = 30;
    private int notificationId = 0;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.e(TAG,"The token string is "  + s);
    }

    // TODO (2) As part of the new Service - Override onMessageReceived. This method will
// be triggered whenever a squawk is received. You can get the data from the squawk
// message using getData(). When you send a test message, this data will include the
// following key/value pairs:
// test: true
// author: Ex. "TestAccount"
// authorKey: Ex. "key_test"
// message: Ex. "Hello world"

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getData() != null){
            Map<String, String> squawkData = remoteMessage.getData();

            //insert test message in DB
            insertSquawk(squawkData);
            sendNotification(squawkData);




        }

    }

    private void insertSquawk(final Map<String, String> data){

        AsyncTask<Void, Void, Void> insertSquawkTask = new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                ContentResolver contentResolver = getContentResolver();
                ContentValues newMessage = new ContentValues();
                newMessage.put(SquawkContract.COLUMN_AUTHOR, data.get(JSON_KEY_AUTHOR));
                newMessage.put(SquawkContract.COLUMN_MESSAGE, data.get(JSON_KEY_MESSAGE).trim());
                newMessage.put(SquawkContract.COLUMN_DATE, data.get(JSON_KEY_DATE));
                newMessage.put(SquawkContract.COLUMN_AUTHOR_KEY, data.get(JSON_KEY_AUTHOR_KEY));

                contentResolver.insert(SquawkProvider.SquawkMessages.CONTENT_URI, newMessage);
                return null;
            }
        };

        insertSquawkTask.execute();
    }

    // TODO (3) As part of the new Service - If there is message data, get the data using
    // the keys and do two things with it :
    // 1. Display a notification with the first 30 character of the message
    // 2. Use the content provider to insert a new message into the local database
    // Hint: You shouldn't be doing content provider operations on the main thread.
    // If you don't know how to make notifications or interact with a content provider
    // look at the notes in the classroom for help.

    private void sendNotification(final Map<String, String> data){

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);


        String message = data.get(JSON_KEY_MESSAGE);
        if(message.length() > NOTIFICATION_MAX_CHARACTERS ){
           message = message.substring(0, 29) + "\u2026" ;
        }
        String author = data.get(JSON_KEY_AUTHOR);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, getResources().getString(R.string.notification_channel_id))
                .setSmallIcon(R.drawable.ic_duck)
                .setContentTitle(String.format(getString(R.string.notification_message), author))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setSound(defaultSoundUri)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, mBuilder.build());
    }


}


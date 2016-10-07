package com.android.living360.app.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.android.living360.R;
import com.android.living360.app.activities.HomeActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Created by Jose on 10/7/2016.
 */

public class PhotoUploadService extends BaseTaskService {

    private static final String TAG = "PhotoUploadService";
    private static final int NOTIF_ID_DOWNLOAD = 0;
    private static final int REQUEST_CODE = 0;

    //Intent Actions///
    private static final String ACTION_UPLOAD = "action_upload";
    public static final String UPLOAD_COMPLETED = "upload_completed";
    public static final String UPLOAD_ERROR = "upload_error";

    /**
     * Intent Extras
     **/
    public static final String EXTRA_FILE_URI = "extra_file_uri";
    public static final String EXTRA_DOWNLOAD_URL = "extra_download_url";

    private StorageReference storageReference;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);
        if (ACTION_UPLOAD.equals(intent.getAction())) {
            Uri fileUri = intent.getParcelableExtra(EXTRA_FILE_URI);
            uploadFromUri(fileUri);
        }

        return START_REDELIVER_INTENT;
    }

    private void uploadFromUri(final Uri fileUri) {

        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        taskStarted();
        showUploadProgressNotification();

        final StorageReference photoRef = storageReference.child("photos")
                .child(fileUri.getLastPathSegment());

        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "uploadFromUri:onSuccess:");

                        Uri downloadUri = taskSnapshot.getMetadata().getDownloadUrl();

                        broadcastUploadFinished(downloadUri, fileUri);
                        showUploadFinishedNotification(downloadUri, fileUri);
                        taskCompleted();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "uploadFromUri:onFailure", e);

                broadcastUploadFinished(null, fileUri);
                showUploadFinishedNotification(null, fileUri);
                taskCompleted();

            }
        });


    }

    private void showUploadProgressNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_upload)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Uploading...")
                .setProgress(0, 0, true)
                .setOngoing(true)
                .setAutoCancel(false);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(NOTIF_ID_DOWNLOAD, builder.build());
    }

    private boolean broadcastUploadFinished(Uri downloadUrl, Uri fileUri) {
        boolean success = downloadUrl != null;

        String action = success ? UPLOAD_COMPLETED : UPLOAD_ERROR;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri);

        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);

    }

    private void showUploadFinishedNotification(Uri downloadUrl, Uri fileUri) {

        Intent intent = new Intent(this, HomeActivity.class)
                .putExtra(EXTRA_DOWNLOAD_URL, downloadUrl)
                .putExtra(EXTRA_FILE_URI, fileUri)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        boolean success = downloadUrl != null;
        String message = success ? "Upload finished" : "Upload Failed";
        int icon = success ? R.drawable.ic_white_check : R.drawable.ic_white_error;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(icon)
                .setContentTitle("Firebase Storage")
                .setContentText(message)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        manager.notify(NOTIF_ID_DOWNLOAD, builder.build());
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UPLOAD_COMPLETED);
        filter.addAction(UPLOAD_ERROR);

        return filter;
    }
}

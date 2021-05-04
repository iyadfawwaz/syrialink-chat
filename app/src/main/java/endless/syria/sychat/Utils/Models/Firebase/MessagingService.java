package endless.syria.sychat.Utils.Models.Firebase;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import java.util.Random;
import endless.syria.sychat.ChatActivity;
import endless.syria.sychat.R;


public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        notification(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {

    }
    private void notification(RemoteMessage remoteMessage){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("sy","sy", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
            RemoteInput remoteInput =new  RemoteInput.Builder("reply")
            .setAllowFreeFormInput(true).build();

        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
       // intent.putExtra("sender","sez");
        //intent.putExtra("reciever","my");
        intent.putExtra("activeUser",remoteMessage.getData().get("sender"));
        PendingIntent pendingIntent = PendingIntent.getActivity(this,703,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action actionx = new NotificationCompat.Action(R.drawable.image_9,"rep",pendingIntent);
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.image_9,"reply",pendingIntent)
      //  .setAllowGeneratedReplies(true)
                .addRemoteInput(remoteInput)
             //   .setShowsUserInterface(true)
                .build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"sy");
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.image_9))
                .setColor(Color.RED)
                .addAction(action)
                .setContentIntent(pendingIntent)
                .setContentTitle(remoteMessage.getData().get("sender"))
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(BitmapFactory.decodeResource(getResources(),R.drawable.image_9)))
                .setAutoCancel(true).setContentText(remoteMessage.getData().get("message")).setSmallIcon(R.drawable.ic_launcher)
                .setVibrate(new long[]{800,200,900,500});
        NotificationManagerCompat managerCompat =  NotificationManagerCompat.from(this);
        managerCompat.notify(new Random().nextInt(),builder.build());
        System.out.println("dd"+remoteMessage.getData().get("sender"));
    }
}

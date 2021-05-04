package endless.syria.sychat;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask.TaskSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import endless.syria.sychat.Utils.Models.Adapters.ChatAdapter;
import endless.syria.sychat.Utils.Models.CircleImageView;
import endless.syria.sychat.Utils.Models.MessageText;


public class ChatActivity extends AppCompatActivity {

    CircleImageView imageProfile,pictureButton,voiceButton;
    EditText messageArea;
    ImageView sendButton;
    Toolbar toolbar;
    TextView username,lastOnline;

    Vibrator vibrator;

    RecyclerView recyclerView;
    LinearLayoutManager linearLayoutManager;

    MediaPlayer mediaPlayer;

    DatabaseReference myReference;
    DatabaseReference userReference;
    FirebaseUser firebaseUser;

    String my,activeUser,uid;
    ArrayList<MessageText> arrayList;
    ChatAdapter adapter;


    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
            setContentView(R.layout.activity_chat);

            sendButton =  findViewById(R.id.sendButton);
            voiceButton =  findViewById(R.id.kmessageCircleImageView);
            pictureButton =  findViewById(R.id.messageCircleImageView);
            messageArea =  findViewById(R.id.messageAra);
            imageProfile =  findViewById(R.id.chatImageprofile);
            toolbar =  findViewById(R.id.activity_Toolbar);
            username =  findViewById(R.id.chatusername);
            lastOnline = findViewById(R.id.laston);
            recyclerView = findViewById(R.id.rec);

            arrayList = new ArrayList<>();
            adapter = new ChatAdapter(arrayList);

            linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setStackFromEnd(true);

            recyclerView.setLayoutManager(linearLayoutManager);
            recyclerView.setAdapter(adapter);
            setSupportActionBar(toolbar);
            toolbar.setClickable(true);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }

            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

            activeUser = getIntent().getStringExtra("activeUser");

            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

            if (firebaseUser != null) {
                my = firebaseUser.getDisplayName();
                uid = firebaseUser.getUid();

            }

            turnOnChat(my, activeUser);

            toolbar.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), activeUser, Toast.LENGTH_LONG).show();
                }

                private void getBigImage(Drawable drawable) {
                    View view = new View(getApplicationContext());
                    view.setBackground(drawable);
                    Dialog dialog = new Dialog(getApplicationContext());
                    dialog.setContentView(view);
                    dialog.setTitle("الصورة");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        dialog.create();
                    }
                    dialog.show();
                }
            });

            imageProfile.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                   bigPicture().show();
                }
            });

            sendButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        String message = messageArea.getText().toString();
                        if (!message.isEmpty()) {
                            @SuppressLint("SimpleDateFormat")
                            MessageText mymessagetext = new MessageText(my, message,new SimpleDateFormat("hh:mm:aa").format(new Date()),0);
                            @SuppressLint("SimpleDateFormat")
                            MessageText usermessagetext = new MessageText(my, message,new SimpleDateFormat("hh:mm:aa").format(new Date()),1);
                            myReference.push().setValue(mymessagetext);
                            userReference.push().setValue(usermessagetext);
                            vibrator.vibrate((long) 300);
                            messageArea.setText("");
                            linearLayoutManager.setStackFromEnd(true);
                            if (recyclerView.getAdapter() != null) {
                                recyclerView.scrollToPosition(recyclerView.getAdapter().getItemCount());
                            }
                            adapter.notifyDataSetChanged();
                        }
                        vibrator.vibrate( 300);
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            username.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    deleteChat(my, activeUser);
                    return false;
                }
            });

            voiceButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Toast.makeText(getApplicationContext(), "غير مدعوم حاليا", Toast.LENGTH_LONG).show();
                }
            });

            pictureButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent("android.intent.action.PICK", Media.EXTERNAL_CONTENT_URI);
                    intent.putExtra("my", my);
                    startActivityForResult(intent, 702);
                }
            });
    }
    private AlertDialog bigPicture(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.big_image,null,false);
        ImageView imageView = view.findViewById(R.id.bigImage);
        imageView.setImageURI(Uri.parse("sdcard/iData/users/"+activeUser+"/"+activeUser+".jpg"));
        builder.setView(view);
        return builder.create();
    }

    private void loadUserLastOnline(final TextView area, final String activeUser){
        FirebaseDatabase.getInstance().getReference("Users/"+activeUser+"/connections")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()){
                            area.setText("متصل الآن");
                        }else {
                            FirebaseDatabase.getInstance().getReference("Users/"+activeUser+"/lastOnline")
                                    .addValueEventListener(new ValueEventListener() {
                                        @SuppressLint("SimpleDateFormat")
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                long x = snapshot.getValue(long.class);
                                                area.setText(new SimpleDateFormat("hh:mm:aa").format(new Date(x)));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void turnOnChat(final String my, String activeUser) {

        String imagePath = "sdcard/iData/users/"+activeUser+"/"+activeUser+".jpg";
        String root = "testData/Users";
        imageProfile.setImageURI(Uri.parse(imagePath));
        myReference = FirebaseDatabase.getInstance().getReference()
                .child(root)
                .child(my)
                .child("messages/userlist")
                .child(activeUser)
                .child("usermessages");
        userReference = FirebaseDatabase.getInstance().getReference()
                .child(root)
                .child(activeUser)
                .child("messages/userlist")
                .child(my)
                .child("usermessages");
        myReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String str) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String str) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @SuppressLint("SimpleDateFormat")
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String str) {
                MessageText messagetext = dataSnapshot.getValue(MessageText.class);
                if (messagetext != null) {
                    String user = messagetext.getUser();
                    String message = messagetext.getMessage();
                    int type = messagetext.getType();
                    arrayList.add(new MessageText(user, message,new SimpleDateFormat("hh:mm:aa").format(new Date()),type));
                    adapter.notifyDataSetChanged();
                }
            }
        });

        username.setText(myReference.child("username").toString());
        username.setText(activeUser);
        loadUserLastOnline(lastOnline,activeUser);
        testImage(imageProfile);
        userLastOnline(firebaseUser.getDisplayName());
           // Picasso.get().load(FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl()).into(imageProfile);
    }
    private void testImage(final ImageView imageView){
        if (imageProfile.getDrawable() == null){
            FirebaseDatabase.getInstance().getReference().child("testData/Users").child(firebaseUser.getDisplayName())
                    .child("uri").addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Picasso.get().load(Uri.parse( snapshot.getValue(String.class))).into(imageView);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    private void userLastOnline(String user){
        final DatabaseReference myConnectionsRef = FirebaseDatabase.getInstance().getReference("Users/"+user+"/connections");
        final DatabaseReference lastOnlineRef = FirebaseDatabase.getInstance().getReference("Users/"+user+"/lastOnline");

       // .onDisconnect().setValue(ServerValue.TIMESTAMP);

                //= FirebaseDatabase.getInstance().getReference("/users/joe/lastOnline");

        final DatabaseReference connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        connectedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean connected = snapshot.getValue(Boolean.class);
                if (connected) {
                    DatabaseReference con = myConnectionsRef.push();
                    con.onDisconnect().removeValue();
                    lastOnlineRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
                    con.setValue(Boolean.TRUE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
        }
    public void deleteChat(String str, String str2) {
        FirebaseDatabase.getInstance().getReference().child("testData/Users").child(str).child("messages/userlist").child(str2).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {

            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(), "تم الحذف بنجاح", Toast.LENGTH_LONG).show();
                }
                finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.middle_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.delx) { /*2131361994*/
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                deleteChat(FirebaseAuth.getInstance().getCurrentUser().getDisplayName(), username.getText().toString());
            }
        }
        return true;
    }

    public static void inserToolBar(Toolbar toolbar, AppCompatActivity actionBarActivity) {

        actionBarActivity.setSupportActionBar(toolbar);
        if (actionBarActivity.getSupportActionBar() != null) {
            actionBarActivity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (req == 701 && data.getExtras() != null) {
            imageProfile.setImageBitmap((Bitmap) data.getExtras().get("data"));
        }
        if (req == 702) {
            if (data != null) {
                getImageSender(data.getStringExtra("my"), username.getText().toString(), data.getData());
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            System.out.println("cv" + bundle.getString("reciever"));
        }
    }

    public void getImageSender(String my, String activeUser, Uri uri) {
        FirebaseStorage instance = FirebaseStorage.getInstance();
        StorageReference child = instance.getReference().child("testData/users/SharedImage/"+my+"/"+activeUser+".jpg");
        StorageReference child2 = instance.getReference().child("testData/users/SharedImage/"+activeUser+"/"+my+".jpg");
        child.putFile(uri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {

            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful() && task.getResult() != null) {
                    Log.d("upload", (task.getResult()).getUploadSessionUri().toString());
                }
            }
        });
        child2.putFile(uri);
        setimageInLayout(uri, 1);
    }

    @SuppressLint("SimpleDateFormat")
    public void setimageInLayout(Uri uri, int i) {
        ImageView imageView = new ImageView(this);
        TextView textView = new TextView(this);
        imageView.setImageURI(uri);
        textView.setText(new SimpleDateFormat("EE:hh:mm:aa").format(new Date(new Date().getTime())));
        textView.setTextSize(2, (float) 19);
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        LayoutParams layoutParams2 = new LayoutParams(-2, -2);
        LayoutParams layoutParams3 = new LayoutParams(-2, -2);
        layoutParams2.weight = 1.0f;
        if (i == 1) {
            layoutParams.gravity = 5;
            layoutParams2.gravity = 5;
            layoutParams3.gravity = 5;
            imageView.setBackgroundColor(-3355444);
        } else {
            layoutParams.gravity = 3;
            layoutParams2.gravity = 3;
            layoutParams3.gravity = 3;
            imageView.setBackgroundColor(-3355444);
        }
        imageView.setLayoutParams(layoutParams2);
        textView.setLayoutParams(layoutParams2);
      //  this.getLayoutInflater().inflate(imageView);
       /// this.layout.addView(textView);
        mediaPlayer = MediaPlayer.create(this, R.raw.gunsound);
        mediaPlayer.start();
       // this.scrollView.fullScroll(ScrollView.FOCUS_DOWN);
    }
}

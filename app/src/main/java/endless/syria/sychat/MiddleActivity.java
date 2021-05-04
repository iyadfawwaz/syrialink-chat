package endless.syria.sychat;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import java.util.ArrayList;
import endless.syria.sychat.Utils.Models.Adapters.MiddleAdapter;
import endless.syria.sychat.Utils.Models.PrefShared;
import endless.syria.sychat.Utils.Models.SignInWithGoolge;


public class MiddleActivity extends AppCompatActivity {

    FirebaseUser fuser;
    DatabaseReference mrefLoader;

    String displayName="";
    String time;

    MiddleAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<MiddleAdapter.SavedMessages> arrayList;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_middle);
        recyclerView = findViewById(R.id.recycler);

        arrayList = new ArrayList<>();
        adapter = new MiddleAdapter(arrayList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        checkSession();

        FirebaseDatabase.getInstance().getReference("scores").keepSynced(true);
        loadAll();

    }
    private void loadAll(){
        arrayList.clear();
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        if (fuser != null) {
            displayName = fuser.getDisplayName();
        }
        UsersLastOnline(displayName);
        mrefLoader = FirebaseDatabase.getInstance().getReference().child("testData").child("Users").child(displayName).child("messages");
        if (displayName == null) {
            Toast.makeText(getApplicationContext(), "no user", Toast.LENGTH_LONG).show();
        } else if (mrefLoader != null) {
            this.mrefLoader.addChildEventListener(new ChildEventListener() {

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

                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String str) {
                    for (DataSnapshot key : dataSnapshot.getChildren()) {
                        final String key2 = key.getKey();
                        final String imgpath = "sdcard/iData/users/" + key2 + "/" + key2 + ".jpg";
                               Uri imgUri = Uri.parse(imgpath);

                            arrayList.add(new MiddleAdapter.SavedMessages(key2, imgUri));
                    }
                    adapter.notifyDataSetChanged();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), "mrefLo", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.addUserToList /*2131361991*/:
                startActivity(new Intent(this,UsersActivity.class));
                break;
            case R.id.aboutme /*2131361992*/:
                startActivity(new Intent(this, AboutActivity.class));
                break;

            case R.id.logoutuser /*2131361993*/:
                logoutUserNow();
                break;
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!getSupportActionBar().isShowing()) {
            getSupportActionBar().show();
        }
        return true;
    }

    public void del() {

        this.fuser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(MiddleActivity.this,"",Toast.LENGTH_LONG).show();
            }
        });
    }

    public void logoutUserNow() {
        FirebaseAuth.getInstance().signOut();
        SignInWithGoolge signInWithGoolge = new SignInWithGoolge().getGoolgeInstance(this);
        final GoogleSignInClient client = signInWithGoolge.getClient();
        client.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                client.revokeAccess().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(), "تم تسجيل الخروج", Toast.LENGTH_LONG).show();
                            finish();
                            System.exit(1);
                        }else {
                            Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
        finish();
    }

    public void infox() {

        Toast.makeText(getApplicationContext(),fuser.getDisplayName()+fuser.getEmail()+fuser.getUid()+"its", Toast.LENGTH_LONG).show();
    }

    public void UsersLastOnline(String str) {
        FirebaseDatabase.getInstance().getReference().child(
                "Users/"+str+"/lastOnline").onDisconnect().setValue(ServerValue.TIMESTAMP);
    }
    @Override
    public void onActivityResult(int i, int i2, Intent intent) {

        super.onActivityResult(i, i2, intent);
        switch (i) {
            case 703:
                String stringExtra = intent.getStringExtra("sender");
                FirebaseDatabase.getInstance().getReference().child("testData").child("Users").child(intent.getStringExtra("reciever")).child("messages").child(stringExtra).child("usermessages");

                Intent intent2 = new Intent(this,ChatActivity.class);
                intent2.putExtra("activeUser", stringExtra);
                intent2.putExtra("imgUri", Uri.parse("sdcard/iData/users/"+stringExtra+"/"+stringExtra+".jpg"));
                startActivity(intent2);

            default:
        }
    }

    public void checkSession() {
        Boolean valueOf = Boolean.valueOf(PrefShared.readSharedPrefs(this, "captionCode", "true"));
        Intent intent = new Intent(this,LoginActivity.class);
        intent.putExtra("cationCode", valueOf);
        if (valueOf) {
            startActivity(intent);
            finish();
        }
    }
}


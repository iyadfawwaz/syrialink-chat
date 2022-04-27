package endless.syria.sychat;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import endless.syria.sychat.Utils.Models.Adapters.UsersAdapter;
import endless.syria.sychat.Utils.Models.CircleImageView;


public class UsersActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private String username;

    private CircleImageView imageView;

    private ArrayList<UsersAdapter.Users> arrayList;
    private UsersAdapter adapter;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
            setContentView(R.layout.activity_users);

        RecyclerView recyclerView = findViewById(R.id.userrecycler);

            arrayList = new ArrayList<>();
            adapter = new UsersAdapter(arrayList);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (firebaseUser != null) {
                username = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
            }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("testData");
            firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            databaseReference.addChildEventListener(new ChildEventListener() {
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

                @SuppressLint("NotifyDataSetChanged")
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String str) {
                    if (dataSnapshot.exists()) {
                        for (DataSnapshot keys : dataSnapshot.getChildren()) {
                            String key = keys.getKey();
                            UsersAdapter.Users users = new UsersAdapter.Users(key);
                            arrayList.add(users);
                            assert key != null;
                            if (key.equals(FirebaseAuth.getInstance().getCurrentUser().getDisplayName())) {
                                arrayList.remove(users);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    }
                }
            });
    }

    public void del() {
        firebaseUser.delete();
        finish();
    }

    public void infox() {
        Toast.makeText(this,firebaseUser.getDisplayName()+firebaseUser.getEmail()+firebaseUser.getUid()+"its", Toast.LENGTH_LONG).show();
    }

    @SuppressLint("SetTextI18n")
    public void setUserInLista(final String str, final String str2) {

    }

    public void saveImageProfileToLocalStoragex(String username) {
        StorageReference fileChild = FirebaseStorage.getInstance().getReference().child("testData/Users/UsersImageProfile/"+username+"/"+username+".jpg");
        fileChild.getDownloadUrl().addOnCompleteListener(this, new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Picasso.get().load(task.getResult()).into(imageView);
            }
        });
       // File file = new File(Environment.getExternalStorageDirectory(), "iData/users/"+username);
       // if (!file.exists()) {
         //   file.mkdirs();

        //    child.getFile(new File(file, username+".jpg")).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {

            //    @Override
              //  public void onSuccess(TaskSnapshot taskSnapshot) {
                //    Toast.makeText(getApplicationContext(), "تم حفظ الصورة", Toast.LENGTH_LONG).show();
              //  }
           // });
        //}
    }

}

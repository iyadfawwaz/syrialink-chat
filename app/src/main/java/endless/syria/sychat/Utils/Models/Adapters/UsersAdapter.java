package endless.syria.sychat.Utils.Models.Adapters;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import endless.syria.sychat.ChatActivity;
import endless.syria.sychat.R;
import endless.syria.sychat.Utils.Models.CircleImageView;
import endless.syria.sychat.Utils.Models.Firebase.Token;


public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersHolder> {

    private final ArrayList<Users> arrayList;

    public UsersAdapter(ArrayList<Users> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public UsersAdapter.UsersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UsersHolder( LayoutInflater.from(parent.getContext()).inflate(R.layout.users_adapter,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull final UsersHolder holder, int position) {
        final DatabaseReference mrefLoader = FirebaseDatabase.getInstance().getReference().child("testData");
       final Users users = arrayList.get(position);
        holder.time.setText(users.getTime());
        holder.activeUser.setText(users.getUser());
        holder.saveImageProfileToLocalStoragex(users.getUser());
        holder.loadUserLastOnline(holder.radioButton,users.getUser());
        holder.activeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mrefLoader.child(users.getUser());
                saveImageProfileToLocalStorage(users.getUser());
                Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
                intent.putExtra("activeUser", users.getUser());
                holder.itemView.getContext().startActivity(intent);
            }

            public void saveImageProfileToLocalStorage(String user) {
                StorageReference child = FirebaseStorage.getInstance().getReference().child("testData/Users/UsersImageProfile/"+user+"/"+user+".jpg");
                File file = new File(Environment.getExternalStorageDirectory(), "iData/users/"+user);
                if (!file.exists()) {
                    file.mkdirs();

                    child.getFile(new File(file, user+".jpg"))
                            .addOnSuccessListener(taskSnapshot ->
                                    Toast.makeText(holder.itemView.getContext(), "تم حفظ الصورة", Toast.LENGTH_LONG).show());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    static class UsersHolder extends RecyclerView.ViewHolder{

        TextView activeUser,time;
        CircleImageView imageView;
        RadioButton radioButton;

        public UsersHolder(@NonNull View itemView) {
            super(itemView);
            activeUser = itemView.findViewById(R.id.utext_message_name);
            time = itemView.findViewById(R.id.utext_message_time);
            imageView = itemView.findViewById(R.id.uimage_message_profile);
            radioButton = itemView.findViewById(R.id.radioButton);

        }
        public void loadUserLastOnline(final RadioButton area, final String activeUser){
            area.setClickable(false);
            FirebaseDatabase.getInstance().getReference("Users/"+activeUser+"/connections")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                area.setText("متصل الآن");
                                area.setChecked(true);
                            }else {
                                FirebaseDatabase.getInstance().getReference("Users/"+activeUser+"/lastOnline")
                                        .addValueEventListener(new ValueEventListener() {
                                            @SuppressLint("SimpleDateFormat")
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    long x = snapshot.getValue(long.class);
                                                    area.setText(new SimpleDateFormat("hh:mm:aa").format(new Date(x)));
                                                    area.setChecked(false);
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

        private void saveImageProfileToLocalStoragex(final String username) {
            StorageReference fileChild = FirebaseStorage.getInstance().getReference().child("testData/Users/UsersImageProfile/"+username+"/"+username+".jpg");
            fileChild.getDownloadUrl()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Picasso.get().load(task.getResult()).into(imageView);
                        }else {
                           FirebaseDatabase.getInstance().getReference().child("testData/Users").child(username).child("uri")
                                   .addChildEventListener(new ChildEventListener() {
                                       @Override
                                       public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                               Picasso.get().load(Uri.parse(snapshot.getValue(String.class))).into(imageView);
                                       }

                                       @Override
                                       public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                           Token token = snapshot.getValue(Token.class);
                                           if (token != null && token.getImageProfileUri() != null) {
                                               Picasso.get().load(token.getImageProfileUri()).into(imageView);
                                           }
                                           imageView.setImageResource(R.drawable.image_4);
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
                    });
        }
    }
    public static class Users{
        private final String user;
        private String time;
        public Users(String user){
            this.user = user;
        }

        public String getUser() {
            return user;
        }

        public String getTime() {
            return time;
        }

    }
}


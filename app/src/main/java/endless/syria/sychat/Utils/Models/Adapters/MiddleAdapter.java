
package endless.syria.sychat.Utils.Models.Adapters;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import endless.syria.sychat.ChatActivity;
import endless.syria.sychat.R;
import endless.syria.sychat.Utils.Models.CircleImageView;
import endless.syria.sychat.Utils.Models.Firebase.Token;


public class MiddleAdapter extends RecyclerView.Adapter<MiddleAdapter.MiddleHolder> {

    private final ArrayList<SavedMessages> arrayList;

    private DatabaseReference databaseReference;

    public MiddleAdapter(ArrayList<SavedMessages> arrayList) {
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public MiddleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MiddleHolder( LayoutInflater.from(parent.getContext()).inflate(R.layout.middle_adapter,parent,false));
    }


    @Override
    public void onBindViewHolder(@NonNull final MiddleHolder holder, int position) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null && firebaseUser.getDisplayName() != null) {
             databaseReference = FirebaseDatabase.getInstance().getReference().child("testData").child("Users")
                    .child(firebaseUser.getDisplayName()).child("messages");
        }
        final SavedMessages savedMessages = arrayList.get(position);

       // Picasso.get().load(savedMessages.getImgUri()).into(holder.imageView);
        Picasso.get().load(Uri.fromFile(new File("sdcard/iData/users/" +savedMessages.getActiveUser() + "/" + savedMessages.getActiveUser() + ".jpg"))).into(holder.imageView);


        holder.loadUserLastOnline(holder.time,holder.radioButton,savedMessages.getActiveUser());
        holder.activeUser.setText(savedMessages.getActiveUser());
        holder.cardView.setOnClickListener(view -> {
            databaseReference.child(savedMessages.getActiveUser()).child("usermessages");
            Intent intent = new Intent(holder.itemView.getContext(), ChatActivity.class);
            intent.putExtra("activeUser", savedMessages.getActiveUser());
            intent.putExtra("imgUri", savedMessages.getImgUri());
            holder.itemView.getContext().startActivity(intent);
        });
        if (holder.imageView.getDrawable()==null){
            FirebaseDatabase.getInstance().getReference().child("testData/Users")
                    .child(savedMessages.getActiveUser()).child("uri")
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                Picasso.get().load(Uri.parse( snapshot.getValue(String.class))).into(holder.imageView);

                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            Token token = snapshot.getValue(Token.class);
                            if (token != null){
                                Picasso.get().load(token.getImageProfileUri()).into(holder.imageView);
                            }else {
                                holder.imageView.setImageResource(R.drawable.image_4);
                            }
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

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    static class MiddleHolder extends RecyclerView.ViewHolder{

        TextView activeUser,time;
        CircleImageView imageView;
        CardView cardView;
        RadioButton radioButton;

        public MiddleHolder(@NonNull View itemView) {
            super(itemView);
            activeUser = itemView.findViewById(R.id.text_message_name);
            time = itemView.findViewById(R.id.text_message_time);
            imageView = itemView.findViewById(R.id.image_message_profile);
            cardView = itemView.findViewById(R.id.cons);
            radioButton = itemView.findViewById(R.id.radioButton2);
        }

        public void loadUserLastOnline(final TextView timex, final RadioButton area, final String activeUser){
            area.setClickable(false);
            FirebaseDatabase.getInstance().getReference("Users/"+activeUser+"/connections")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()){
                                timex.setText("متصل الآن");
                                area.setVisibility(View.VISIBLE);
                                area.setChecked(true);
                            }else {
                                FirebaseDatabase.getInstance().getReference("Users/"+activeUser+"/lastOnline")
                                        .addValueEventListener(new ValueEventListener() {
                                            @SuppressLint("SimpleDateFormat")
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.exists()) {
                                                    //noinspection ConstantConditions
                                                    long x = snapshot.getValue(long.class);
                                                    timex.setText(new SimpleDateFormat("hh:mm:aa").format(new Date(x)));
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

    }
    public static class SavedMessages{
        private final String activeUser;
        private final Uri imgUri;
        public SavedMessages(String activeUser,Uri imgUri){
            this.activeUser = activeUser;
            this.imgUri = imgUri;
        }

        public String getActiveUser() {
            return activeUser;
        }

        public Uri getImgUri() {
            return imgUri;
        }
    }
}

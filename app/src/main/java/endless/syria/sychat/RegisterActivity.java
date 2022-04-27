package endless.syria.sychat;


import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest.Builder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


public class RegisterActivity extends AppCompatActivity {

    public static final int IMAGE_REQUESTER=202;

    private Button closeButton;
    private EditText eemail;
    private EditText epass;
    private EditText euser;
    private ImageView imageView;

    private Uri imgUri;

    Vibrator vibrator;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
            setContentView(R.layout.activity_register);

            closeButton =  findViewById(R.id.regButton2);
        Button breg = findViewById(R.id.regButton1);
            euser =  findViewById(R.id.regEditText1);
            eemail =  findViewById(R.id.regEditText2);
            epass =  findViewById(R.id.regEditText3);
            imageView =  findViewById(R.id.regImageView1);

            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

            firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("testData").child("Users");

            closeButton.setOnClickListener(view -> finish());

            imageView.setOnClickListener(view -> {
                Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
                intent.addCategory("android.intent.category.OPENABLE");
                intent.setType("image/*");
                startActivityForResult(intent, IMAGE_REQUESTER);
            });

            breg.setOnClickListener(view -> {

                final String username = euser.getText().toString();
                final String email = eemail.getText().toString();
                final String password = epass.getText().toString();
                if (username.isEmpty()) {
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{400},1));
                    Toast.makeText(getApplicationContext(), "ادخل اسم المستخدم", Toast.LENGTH_LONG).show();
                } else if (email.isEmpty()) {

                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{300},1));

                    Toast.makeText(getApplicationContext(), "ادخل بريد صالح", Toast.LENGTH_LONG).show();
                } else if (password.isEmpty()) {
                    vibrator.vibrate(VibrationEffect.createWaveform(new long[]{300},1));
                    Toast.makeText(getApplicationContext(), "ادخل كلمة مرور", Toast.LENGTH_LONG).show();
                } else if (firebaseAuth != null) {
                    databaseReference.child(username).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }

                        @Override
                        public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                Toast.makeText(getApplicationContext(), "هذا المستخدم موجود جرب اسم اخر", Toast.LENGTH_LONG).show();
                            } else {

                                firebaseAuth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()) {

                                                FirebaseInstallations.getInstance().getId().addOnCompleteListener(RegisterActivity.this,
                                                        task1 -> {
                                                            if (task1.isSuccessful() && task1.getResult() != null)
                                                                databaseReference.child(username).child("uri")
                                                                        .setValue(imgUri.toString());
                                                        });
                                                firebaseUser = firebaseAuth.getCurrentUser();
                                                if (firebaseUser != null) {
                                                    firebaseUser.updateProfile(
                                                            new Builder().setDisplayName(username).setPhotoUri(imgUri).build());

                                                    firebaseUser.sendEmailVerification();
                                                    Toast.makeText(getApplicationContext(), "قمنا بارسال رمز التفعيل الى  : "
                                                            + firebaseUser.getEmail(), Toast.LENGTH_LONG).show();
                                                    finish();
                                                }
                                            } else {
                                                if (task.getException() != null) {
                                                    closeButton.setText(task.getException().getMessage());
                                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                            }
                        }
                    });
                } else {
                    Toast.makeText(getApplicationContext(), "خطأ بالولوج", Toast.LENGTH_LONG).show();
                }
            });
    }

    @Override
    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (data != null) {

            storageReference = FirebaseStorage.getInstance().getReference()
                    .child("testData/Users/UsersImageProfile/" + euser.getText() + "/" + euser.getText().toString() + ".jpg");
            if (data.getData() != null) {

                        storageReference.putFile(data.getData()).addOnSuccessListener(taskSnapshot ->
                                storageReference.getDownloadUrl().addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                imgUri = task.getResult();
                                Picasso.get().load(imgUri).into(imageView);

                                Toast.makeText(getApplicationContext(),task.getResult().toString(),Toast.LENGTH_LONG).show();
                            }else {
                                if (task.getException() != null) {
                                    euser.setText(task.getException().getMessage());
                                }
                            }
                        }));
            }
        }
    }

    @Override
    public void onBackPressed() {

        super.onBackPressed();
    }

}

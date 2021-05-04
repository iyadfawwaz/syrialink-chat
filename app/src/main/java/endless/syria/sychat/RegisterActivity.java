package endless.syria.sychat;


import android.app.ProgressDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest.Builder;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.installations.FirebaseInstallations;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask.TaskSnapshot;
import com.squareup.picasso.Picasso;


public class RegisterActivity extends AppCompatActivity {

    public static final int IMAGE_REQUESTER=202;

    Button breg,closeButton;
    EditText eemail;
    EditText epass;
    EditText euser;
    ImageView imageView;

    Uri imgUri;

    Vibrator vibrator;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    DatabaseReference databaseReference;
    StorageReference storageReference;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
            setContentView(R.layout.activity_register);

            closeButton =  findViewById(R.id.regButton2);
            breg =  findViewById(R.id.regButton1);
            euser =  findViewById(R.id.regEditText1);
            eemail =  findViewById(R.id.regEditText2);
            epass =  findViewById(R.id.regEditText3);
            imageView =  findViewById(R.id.regImageView1);

            vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

            firebaseAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("testData").child("Users");

            closeButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            imageView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent("android.intent.action.OPEN_DOCUMENT");
                    intent.addCategory("android.intent.category.OPENABLE");
                    intent.setType("image/*");
                    startActivityForResult(intent, IMAGE_REQUESTER);
                }
            });
            breg.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                   MediaPlayer mediaPlayer = new MediaPlayer();
                    final String username = euser.getText().toString();
                    final String email = eemail.getText().toString();
                    final String password = epass.getText().toString();
                    if (username.isEmpty()) {
                        vibrator.vibrate(300);
                        Toast.makeText(getApplicationContext(), "ادخل اسم المستخدم", Toast.LENGTH_LONG).show();
                    } else if (email.isEmpty()) {
                        vibrator.vibrate(300);
                        Toast.makeText(getApplicationContext(), "ادخل بريد صالح", Toast.LENGTH_LONG).show();
                    } else if (password.isEmpty()) {
                        vibrator.vibrate(300);
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
                                            .addOnCompleteListener(  new OnCompleteListener<AuthResult>() {

                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {

                                                FirebaseInstallations.getInstance().getId().addOnCompleteListener(RegisterActivity.this,new OnCompleteListener<String>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<String> task) {
                                                        if (task.isSuccessful() && task.getResult() != null)
                                                            databaseReference.child(username).child("uri")
                                                                    .setValue(imgUri.toString());
                                                    }
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
                                        }
                                    });
                                }
                            }
                        });
                    } else {
                        Toast.makeText(getApplicationContext(), "خطأ بالولوج", Toast.LENGTH_LONG).show();
                    }
                }
            });
    }

    @Override
    public void onActivityResult(int req, int res, Intent data) {
        super.onActivityResult(req, res, data);
        if (data != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.show();
            storageReference = FirebaseStorage.getInstance().getReference()
                    .child("testData/Users/UsersImageProfile/" + euser.getText() + "/" + euser.getText().toString() + ".jpg");
            if (data.getData() != null) {

                        storageReference.putFile(data.getData()).addOnSuccessListener(new OnSuccessListener<TaskSnapshot>() {
                    @Override
                    public void onSuccess(TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()){
                                    imgUri = task.getResult();
                                    Picasso.get().load(imgUri).into(imageView);
                                    progressDialog.dismiss();
                                    Toast.makeText(getApplicationContext(),task.getResult().toString(),Toast.LENGTH_LONG).show();
                                }else {
                                    if (task.getException() != null) {
                                        euser.setText(task.getException().getMessage());
                                    }
                                }
                            }
                        });

                       // Toast.makeText(getApplicationContext(), taskSnapshot.getUploadSessionUri().toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
    }

}

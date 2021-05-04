package endless.syria.sychat;


import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.squareup.picasso.Picasso;
import endless.syria.sychat.Utils.Models.CircleImageView;
import endless.syria.sychat.Utils.Models.SignInWithGoolge;


public class AboutActivity extends AppCompatActivity {

    TextView username,email,uid;

    CircleImageView circleImageView;

    TextView dispalyName,displayEmail,dispalyUid;

    Button button;

    public FirebaseUser firebaseUser;
    public FirebaseAuth firebaseAuth;

    String displayNames,uids,emails;

    Uri photoUrls;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_aboutme);

        dispalyName =  findViewById(R.id.usernamex);
        displayEmail =  findViewById(R.id.emailx);
        dispalyUid =  findViewById(R.id.uidx);
        username =  findViewById(R.id.usernamea);
        email =  findViewById(R.id.emaila);
        uid =  findViewById(R.id.uida);
        button = findViewById(R.id.deletebt);

        circleImageView =  findViewById(R.id.aCircleImageView);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        if (firebaseUser != null) {
             displayNames = firebaseUser.getDisplayName();
             emails = firebaseUser.getEmail();
             uids = firebaseUser.getUid();
             photoUrls = firebaseUser.getPhotoUrl();
        }
        username.setText("اسم المستخدم");
        dispalyName.setText(displayNames);
        email.setText("البريد الالكتروني");
        displayEmail.setText(emails);
        uid.setText("المعرف الشخصي");
        dispalyUid.setText(uids);
        if (photoUrls==null) {
            circleImageView.setImageResource(R.drawable.image_4);
        } else {
            Picasso.get().load(photoUrls).into(circleImageView);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    firebaseUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @SuppressLint("ShowToast")
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                new SignInWithGoolge().getGoolgeInstance(AboutActivity.this).getClient().signOut();
                                new SignInWithGoolge().getGoolgeInstance(AboutActivity.this).getClient().revokeAccess();
                              //  firebaseAuth.signOut();
                                Toast.makeText(getApplicationContext(), "تم حذف الحساب بنجاح", Toast.LENGTH_LONG).show();
                                finish();
                                System.exit(5);
                            } else {
                                if (!task.isSuccessful() && task.getException() != null) {
                                    new SignInWithGoolge().getGoolgeInstance(AboutActivity.this).getClient().silentSignIn()
                                            .addOnCompleteListener(new OnCompleteListener<GoogleSignInAccount>() {
                                                @Override
                                                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {

                                                    AuthCredential credential = GoogleAuthProvider.getCredential(task.getResult().getIdToken(),null);
                                                    FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            new SignInWithGoolge().getGoolgeInstance(AboutActivity.this).getClient().revokeAccess();
                                                                            Toast.makeText(getApplicationContext(),"deleted",Toast.LENGTH_LONG).show();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                }
                                            });


                                }
                            }
                        }
                    });
                }
            }
        });
    }
}


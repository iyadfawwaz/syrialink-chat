
package endless.syria.sychat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import endless.syria.sychat.Utils.Models.PrefShared;
import endless.syria.sychat.Utils.Models.SignInWithGoolge;


public class LoginActivity extends AppCompatActivity {

    EditText email,password;
    Button login,google;
    TextView register,reset,warring;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    MediaPlayer mediaPlayer;

    Vibrator vibrator;

    private GoogleSignInClient client;
    private SignInWithGoolge signInWithGoolge;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pubg);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        login = findViewById(R.id.login);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        reset = findViewById(R.id.res);
        register = findViewById(R.id.reg);
        warring = findViewById(R.id.loginTextView1);
        google = findViewById(R.id.google);

        firebaseAuth = FirebaseAuth.getInstance();
        mediaPlayer.start();
        requestPermissions(new String[]{
        Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},1);
        google.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              signInWithGoolge = new SignInWithGoolge().getGoolgeInstance(LoginActivity.this);
                     client = signInWithGoolge.getClient();
              Intent intent = client.getSignInIntent();
              signInWithGoolge.startSignInActivity();
            }
        });

        login.setOnClickListener(new OnClickListener() {
                                     @Override
                                     public void onClick(View view) {
                                         String mail = email.getText().toString();
                                         String pass = password.getText().toString();
                                         if (mail.isEmpty()) {
                                             vibrator.vibrate(300);
                                             warningAlert("ادخل البريد");
                                         } else if (pass.isEmpty()) {
                                             warningAlert("ادخل كلمة السر");
                                         } else {
                                             firebaseAuth.signInWithEmailAndPassword(mail, pass)
                                                     .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                                         @Override
                                                         public void onComplete(@NonNull Task<AuthResult> task) {
                                                             if (task.isSuccessful() && task.getResult() != null) {
                                                                 firebaseUser = task.getResult().getUser();
                                                                 if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                                                                     mediaPlayer.release();
                                                                     PrefShared.saveSharedPrefs((getApplicationContext()), "captionCode", "false");
                                                                     Intent intent = new Intent(LoginActivity.this, MiddleActivity.class);
                                                                     intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                                                     startActivity(intent);
                                                                     finish();
                                                                 } else {
                                                                     warningAlert("من فضلك قم بتاكيد بريدك");

                                                                 }
                                                             } else {
                                                                 if (!task.isSuccessful() && task.getException() != null
                                                                 && task.getException().getMessage() != null) {
                                                                     warningAlert(task.getException().getMessage());
                                                                 }
                                                                 else {
                                                                     warningAlert("task failed");
                                                                 }
                                                             }
                                                         }
                                                     });
                                         }
                                     }
                                 });

        register.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.release();
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
                finish();
            }
        });

        reset.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.release();
                startActivity(new Intent(getApplicationContext(), ResetActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null){
            if (requestCode == SignInWithGoolge.SIGNIN_REQ_ID){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    final GoogleSignInAccount account = task.getResult(ApiException.class);
                        PrefShared.GOOGLE_TOKEN = account.getIdToken();
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                               if (task.getResult() != null && task.getResult().getUser() != null) {
                                  FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(
                                          new OnCompleteListener<InstanceIdResult>() {
                                      @Override
                                      public void onComplete(@NonNull Task<InstanceIdResult> task) {
                                          if (task.isSuccessful() && task.getResult() != null && account.getPhotoUrl() != null && account.getDisplayName() != null) {

                                              FirebaseDatabase.getInstance().getReference().child("testData/Users").child(account.getDisplayName())
                                                      .child("uri").setValue(account.getPhotoUrl().toString());
                                          }
                                      }
                                  });
                                   mediaPlayer.stop();
                                   mediaPlayer.release();
                                   startActivity(new Intent(LoginActivity.this, MiddleActivity.class));
                               }
                            }
                        }
                    });
                } catch (ApiException e) {
                    warring.setText(e.getMessage());
                    warningAlert(e.getMessage());
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser!=null){
            if (firebaseUser.isEmailVerified()){
                mediaPlayer.stop();
                mediaPlayer.release();
                PrefShared.saveSharedPrefs(this, "captionCode", "false");
                Intent intent = new Intent(this,MiddleActivity.class);
                startActivity(intent);
                finish();
            }else {
                warningAlert("من فضلك قم بتأكيد بريدك");
            }
        }
    }

    public void warningAlert( @NonNull String error) {
        if (error.length() > 0) {
            Toast makeText = Toast.makeText(getApplicationContext(), error, Toast.LENGTH_LONG);
            makeText.setGravity(49, 0, 0);
            View view = makeText.getView();
            assert view != null;
            view.setBackgroundColor(Color.BLUE);
            view.setTop(500);
            makeText.setView(view);
            makeText.show();
        }
    }
}

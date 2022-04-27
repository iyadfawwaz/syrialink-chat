
package endless.syria.sychat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import endless.syria.sychat.Utils.Models.PrefShared;
import endless.syria.sychat.Utils.Models.SignInWithGoolge;
import endless.syria.sychat.Utils.Models.SyriaChatException;


public class LoginActivity extends AppCompatActivity {

    private final static int REQUEST =209;
    private EditText email,password;
    private TextView warring;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;

    private MediaPlayer mediaPlayer;

    private Vibrator vibrator;

    private CallbackManager callbackManager;


    private SignInWithGoolge signInWithGoolge;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);

        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.pubg);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        Button login = findViewById(R.id.login);
        LoginButton loginButton = findViewById(R.id.flogin);
        Button github = findViewById(R.id.github);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        TextView reset = findViewById(R.id.res);
        TextView register = findViewById(R.id.reg);
        warring = findViewById(R.id.loginTextView1);
        Button google = findViewById(R.id.google);

        firebaseAuth = FirebaseAuth.getInstance();
        mediaPlayer.start();

        requestPermissions(new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST);

        github.setOnClickListener(
                view -> firebaseAuth.startActivityForSignInWithProvider(this,
                OAuthProvider.newBuilder("github",firebaseAuth).build())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        try {
                            FirebaseUser user = task.getResult().getUser();
                            assert user != null;
                                String uri = String.valueOf(user.getPhotoUrl());
                            assignInformations(user.getDisplayName(), uri);
                        } catch (SyriaChatException e) {
                            warningAlert(e.getMessage());
                        }
                    }else {
                        if (task.getException() != null){
                            warningAlert(task.getException().getMessage());
                        }
                    }
                }));
        google.setOnClickListener(v -> {
          signInWithGoolge = new SignInWithGoolge().getGoolgeInstance(LoginActivity.this);

            try {
                signInWithGoolge.startSignInActivity();
            } catch (SyriaChatException e) {
                warningAlert(e.getMessage());
            }
        });

        login.setOnClickListener(view -> {
            String mail = email.getText().toString();
            String pass = password.getText().toString();
            if (mail.isEmpty()) {
                vibrator.vibrate(VibrationEffect.createOneShot(300,VibrationEffect.DEFAULT_AMPLITUDE));
                warningAlert("ادخل البريد");
            } else if (pass.isEmpty()) {
                warningAlert("ادخل كلمة السر");
            } else {
                firebaseAuth.signInWithEmailAndPassword(mail, pass)
                        .addOnCompleteListener(LoginActivity.this, task -> {
                            if (task.isSuccessful() && task.getResult() != null) {
                                firebaseUser = task.getResult().getUser();
                                if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                                    mediaPlayer.release();
                                    PrefShared.saveSharedPrefs((getApplicationContext()), "captionCode", "false");
                                    Intent intent = new Intent(LoginActivity.this, MiddleActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                    startActivity(intent);
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
                        });
            }
        });

        register.setOnClickListener(view -> {
            mediaPlayer.release();
            startActivity(new Intent(getApplicationContext(), RegisterActivity.class));

        });

        reset.setOnClickListener(view -> {
            mediaPlayer.release();
            startActivity(new Intent(getApplicationContext(), ResetActivity.class));

        });

        loginButton.setPermissions("public_profile","email");
            callbackManager = CallbackManager.Factory.create();
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {

                    AuthCredential credential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                    firebaseAuth.signInWithCredential(credential).addOnCompleteListener(task ->
                    {
                        if (task.isSuccessful()){
                            try {
                                FirebaseUser user = task.getResult().getUser();
                                assert user != null;
                                String displayName = user.getDisplayName();
                                assert user.getPhotoUrl() != null;
                                String imageUri = String.valueOf(user.getPhotoUrl());
                                assignInformations(displayName, imageUri);
                            } catch (SyriaChatException e) {
                                warningAlert(e.getMessage());
                            }
                            startActivity(new Intent(LoginActivity.this,MiddleActivity.class));
                        }else {
                            if (task.getException() != null)
                                warningAlert(task.getException().getMessage());
                        }
                    });
                }

                @Override
                public void onCancel() {

                    warningAlert("canceled...");
                }

                @Override
                public void onError(@NonNull FacebookException e) {

                    warningAlert(e.getMessage());
                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode,resultCode,data);

        if (data != null){
            if (requestCode == SignInWithGoolge.SIGNIN_REQ_ID){
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    final GoogleSignInAccount account = task.getResult(ApiException.class);
                        PrefShared.GOOGLE_TOKEN = account.getIdToken();
                    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
                    FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()){
                           if (task1.getResult() != null && task1.getResult().getUser() != null) {
                              FirebaseMessaging.getInstance().getToken().addOnCompleteListener(
                                      task11 -> {
                                          if (task11.isSuccessful() && task11.getResult() != null && account.getPhotoUrl() != null && account.getDisplayName() != null) {

                                              try {
                                                  assignInformations(account.getDisplayName(),account.getPhotoUrl().toString());
                                              } catch (SyriaChatException e) {
                                                  warningAlert(e.getMessage());
                                              }
                                          }
                                      });
                               mediaPlayer.release();
                               startActivity(new Intent(LoginActivity.this, MiddleActivity.class));
                           }
                        }
                    });
                } catch (ApiException e) {
                    warring.setText(e.getMessage());
                    if (e.getMessage() != null)
                    warningAlert(e.getMessage());
                }
            }
        }
    }

    private void assignInformations(String displayName, String imageUri) throws SyriaChatException{
        if (displayName == null){
            throw new SyriaChatException("اسم المستخدم فارغ");
        }
        if (imageUri == null){
            throw new SyriaChatException("عنوان الصورة خاطئ");
        }
        FirebaseDatabase.getInstance().getReference().child("testData/Users")
                .child(displayName).child("uri").setValue(imageUri);
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser!=null){
            if (firebaseUser.isEmailVerified()){
               // mediaPlayer.stop();
                mediaPlayer.release();
                PrefShared.saveSharedPrefs(this, "captionCode", "false");
                Intent intent = new Intent(this,MiddleActivity.class);
                startActivity(intent);
            }else {
                warningAlert("من فضلك قم بتأكيد بريدك");
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer == null){
            mediaPlayer = MediaPlayer.create(this,R.raw.pubg);
        }

        mediaPlayer.start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mediaPlayer.pause();
    }

    public void warningAlert(String error) {
        TextView textView = new TextView(this);
        textView.setTextIsSelectable(true);
        textView.setTextAppearance(R.style.AlertDialog_AppCompat_Light);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setCancelable(true)
                .setIcon(R.drawable.image_2)
                .setTitle(getString(R.string.app_name))
                .setView(textView);

            textView.setText(error);
            builder.setMessage(error);
        builder.create().show();
    }
}

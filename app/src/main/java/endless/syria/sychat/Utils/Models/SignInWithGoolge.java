package endless.syria.sychat.Utils.Models;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import endless.syria.sychat.MiddleActivity;
import endless.syria.sychat.R;


public class SignInWithGoolge {

    private Activity activity;
    public static final int SIGNIN_REQ_ID=202;
    @SuppressLint("StaticFieldLeak")
    private GoogleSignInClient client;

    public SignInWithGoolge(){
        super();
    }
    public  SignInWithGoolge getGoolgeInstance(Activity activity) {
        this.activity = activity;
        return this;
    }
    public GoogleSignInClient getClient(){
        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .requestIdToken(activity.getString(R.string.banner_ad_unit_id))
                .build();
        client = GoogleSignIn.getClient(activity,options);
        return client;
    }
    public void startSignInActivity(){

        activity.startActivityForResult(client.getSignInIntent(),SIGNIN_REQ_ID);
    }

    public void signInFirebase(Intent intent) throws ApiException {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
        final GoogleSignInAccount account = task.getResult(ApiException.class);
        if ( account != null ) {
            GoogleAuthCredential credential = (GoogleAuthCredential) GoogleAuthProvider.getCredential(account.getIdToken(), null);
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(
                    new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                    .child("/testData/Users/UsersImageProfile/" + account.getDisplayName() + "/" + account.getDisplayName() + ".jpg");
                            if (account.getPhotoUrl() != null) {
                                try {
                                    InputStream inputStream = new FileInputStream(new File(account.getPhotoUrl().getPath()));
                                    storageReference.putFile(account.getPhotoUrl())
                                            .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(activity, task.getResult().getUploadSessionUri().toString(), Toast.LENGTH_LONG).show();
                                                    } else {
                                                        Toast.makeText(activity, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                } catch (FileNotFoundException e) {
                                    Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                                activity.startActivity(new Intent(activity, MiddleActivity.class));
                            }
                        }
                    });
        }
    }
    public void signOut(){
        getClient().signOut();
    }
}

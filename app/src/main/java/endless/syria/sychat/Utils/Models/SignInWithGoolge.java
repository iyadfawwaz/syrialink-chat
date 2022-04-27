package endless.syria.sychat.Utils.Models;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthCredential;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
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
    public SignInWithGoolge getFacebookInstance(Context context){
        FacebookSdk.setApplicationId(context.getString(R.string.facebook_app_id));
        FacebookSdk.setClientToken(context.getString(R.string.facebook_client_token));
        CallbackManager callbackManager = CallbackManager.Factory.create();
        return this;
    }
public  SignInWithGoolge getGoolgeInstance(Activity activity) {
        this.activity = activity;
        return this;
    }
    public GoogleSignInClient initializeClient() throws SyriaChatException {

        GoogleSignInOptions options = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .requestEmail()
                .requestIdToken(activity.getString(R.string.default_web_client_idx))
                .build();

        client = GoogleSignIn.getClient(activity,options);
        return client;
    }
    public void startSignInActivity() throws SyriaChatException{

        if (initializeClient() == null){
            throw new SyriaChatException("مشكلة في صفخة الدخول");
        }
        activity.startActivityForResult(initializeClient().getSignInIntent(),SIGNIN_REQ_ID);
    }

    public void signInFirebase(Intent intent) throws ApiException {
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(intent);
        final GoogleSignInAccount account = task.getResult(ApiException.class);
        if ( account != null ) {
            GoogleAuthCredential credential = (GoogleAuthCredential) GoogleAuthProvider.getCredential(account.getIdToken(), null);
            FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener(
                    task1 -> {
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference()
                                .child("/testData/Users/UsersImageProfile/" + account.getDisplayName() + "/" + account.getDisplayName() + ".jpg");
                        if (account.getPhotoUrl() != null) {
                            storageReference.putFile(account.getPhotoUrl())
                                    .addOnCompleteListener(task11 -> {
                                        if (task11.isSuccessful()) {
                                            Toast.makeText(activity, (CharSequence) task11.getResult().getUploadSessionUri(), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(activity, (CharSequence) task11.getException(), Toast.LENGTH_LONG).show();
                                        }
                                    });
                            activity.startActivity(new Intent(activity, MiddleActivity.class));
                        }
                    });
        }
    }
    public void signOut() throws SyriaChatException {
        initializeClient().signOut();
    }
}

package endless.syria.sychat.Utils.Models.Firebase;


import android.net.Uri;
import androidx.annotation.NonNull;


public class Token {
    private String token;
    private Uri imageProfileUri;
    private Token(){

    }
    public Token(@NonNull String token,@NonNull Uri imageProfileUri){
        this.token = token;
        this.imageProfileUri = imageProfileUri;
    }

    public Uri getImageProfileUri() {
        return imageProfileUri;
    }

    public void setImageProfileUri(@NonNull Uri imageProfileUri) {
        this.imageProfileUri = imageProfileUri;
    }

    public String getToken() {
        return token;
    }

    public void setToken(@NonNull String token) {
        this.token = token;
    }
}

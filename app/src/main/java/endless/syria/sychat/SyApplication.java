package endless.syria.sychat;


import android.app.Application;
import com.google.firebase.database.FirebaseDatabase;


public class SyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}

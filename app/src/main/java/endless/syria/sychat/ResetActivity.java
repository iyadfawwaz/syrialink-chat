package endless.syria.sychat;


import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;


public class ResetActivity extends AppCompatActivity {

    EditText email;
    Button send;

    public Vibrator vibrate;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        try {
            setContentView(R.layout.activity_reset);
            this.send =  findViewById(R.id.resetb);
            this.email =  findViewById(R.id.emailres);
            this.send.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    FirebaseAuth instance = FirebaseAuth.getInstance();
                    String editable = email.getText().toString();
                    if (editable.isEmpty()) {
                        vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                        vibrate.vibrate((long) 300);
                        warr("ادخل بريدك");
                    }
                    instance.sendPasswordResetEmail(editable);
                    warr("email sent success");
                }
            });
        } catch (Exception e) {
            warr(e.getMessage());
        }
    }

    public void warr( String str) {
        Toast makeText = Toast.makeText(this, str, Toast.LENGTH_LONG);
        makeText.setGravity(49, 0, 0);
        View view = makeText.getView();
        if (view != null) {
            view.setBackgroundColor(Color.BLUE);
            view.setTop(500);
        }
        makeText.setView(view);
        makeText.show();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

}

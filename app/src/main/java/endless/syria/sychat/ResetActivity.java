package endless.syria.sychat;


import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;


public class ResetActivity extends AppCompatActivity {

    private EditText email;

    private Vibrator vibrate;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        try {
            setContentView(R.layout.activity_reset);

            Button send = findViewById(R.id.resetb);
            email =  findViewById(R.id.emailres);

            send.setOnClickListener(view -> {
                FirebaseAuth instance = FirebaseAuth.getInstance();
                String editable = email.getText().toString();
                if (editable.isEmpty()) {
                    vibrate = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    vibrate.vibrate(VibrationEffect.createWaveform(new long[]{300},1));
                    warr("ادخل بريدك");
                }
                instance.sendPasswordResetEmail(editable);
                warr("email sent success");
            });
        } catch (Exception e) {
            warr(e.getMessage());
        }
    }

    public void warr( String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.app_name))
                .setIcon(R.drawable.image_2)
                .setCancelable(true);
        if (error != null) {
            builder.setMessage(error);
        }
       builder.create().show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

}

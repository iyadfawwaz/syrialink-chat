package endless.syria.sychat.Utils.Models;


import android.content.Context;
import android.content.SharedPreferences;


public class PrefShared {
        static final String FileName = "captionCode";
        public static String GOOGLE_TOKEN;


        public static String readSharedPrefs(Context context, String str, String str2) {
            return  context.getSharedPreferences(FileName, 0).getString(str, str2);
        }

        public static void saveSharedPrefs(Context context, String str, String str2) {
            SharedPreferences.Editor edit = context.getSharedPreferences(FileName, 0).edit();
            edit.putString(str, str2);
            edit.apply();
        }
    }


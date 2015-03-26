
package info.guardianproject.locationprivacy;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Prefs {

    // fake packageNames that represent no app, or the app chooser
    public static final String BLOCKED_NAME = "BLOCKED";
    public static final String CHOOSER_NAME = "CHOOSER";

    public static final String TRUSTED_APP_PREF = "TRUSTED_APP_PREF";
    public static final String TRUSTED_APP_PREF_DEFAULT = CHOOSER_NAME;

    private static SharedPreferences prefs;

    static void setup(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static boolean contains(String key) {
        return prefs.contains(key);
    }

    /**
     * Get the {@code packageName} of the long-term preferred app
     */
    public static String getTrustedApp() {
        return prefs.getString(TRUSTED_APP_PREF, TRUSTED_APP_PREF_DEFAULT);
    }

    /**
     * Set the long-term preferred app by {@code packageName}
     */
    public static void setTrustedApp(String packageName) {
        prefs.edit().putString(TRUSTED_APP_PREF, packageName).apply();
    }
}

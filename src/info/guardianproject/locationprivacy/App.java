
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class App extends Application {
    public static final String TAG = "LocationPrivacy";

    // preferred trusted map app
    public static final String OSMAND_FREE = "net.osmand";
    public static final String OSMAND_PLUS = "net.osmand.plus";
    public static final int ORBOT_START_RESULT = 0x04807;
    private static String selectedPackageName;

    private PackageMonitor packageMonitor;

    @Override
    public void onCreate() {
        Prefs.setup(this);
        packageMonitor = new PackageMonitor();
        packageMonitor.register(this, true);
        super.onCreate();
    }

    public static void startActivityWithTrustedApp(Activity activity, Intent intent) {
        if (TextUtils.equals(selectedPackageName, Prefs.BLOCKED_NAME)) {
            Toast.makeText(activity, R.string.blocked_url, Toast.LENGTH_SHORT).show();
        } else {
            intent.setComponent(null); // prompt user for Activity to view URI
            if (!TextUtils.equals(selectedPackageName, Prefs.CHOOSER_NAME)) {
                // narrow down the choice of Activities to this packageName
                intent.setPackage(selectedPackageName);
            }
            Log.i(TAG, "startActivityWithTrustedApp " + intent.getData() + " " + intent.getPackage());
            startActivity(activity, intent);
        }
    }

    public static void startActivityWithBlockedOrChooser(Activity activity, Intent intent) {
        if (TextUtils.equals(selectedPackageName, Prefs.BLOCKED_NAME)) {
            Toast.makeText(activity, R.string.blocked_url, Toast.LENGTH_SHORT).show();
        } else {
            intent.setComponent(null); // prompt user for Activity to view URI
            Toast.makeText(activity, R.string.ignoring_unparsable_url, Toast.LENGTH_LONG).show();
            startActivity(activity, intent);
        }
    }

    private static void startActivity(Activity activity, Intent intent) {
        try {
            activity.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(activity, R.string.no_geo_map_app, Toast.LENGTH_LONG).show();
            // show the main screen to walk the user through installing Osmand
            activity.startActivity(new Intent(activity, MainActivity.class));
        }
    }

    /**
     * Set the currently active map app by {@code packageName}
     */
    public static void setSelectedPackageName(String packageName) {
        selectedPackageName = packageName;
    }

    /**
     * Get the {@code packageName} of the currently active map app
     */
    public static String getSelectedPackageName() {
        return selectedPackageName;
    }
}

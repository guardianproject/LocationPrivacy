
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import info.guardianproject.onionkit.ui.OrbotHelper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class App extends Application {

    // preferred trusted map app
    public static final String OSMAND_FREE = "net.osmand";
    public static final String OSMAND_PLUS = "net.osmand.plus";
    public static final int ORBOT_START_RESULT = 0x04807;
    private static String selectedPackageName;
    static OrbotHelper orbotHelper;

    private PackageMonitor packageMonitor;

    @Override
    public void onCreate() {
        Prefs.setup(this);
        packageMonitor = new PackageMonitor();
        packageMonitor.register(this, true);
        orbotHelper = new OrbotHelper(getApplicationContext());
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
            Log.i("startActivityWithTrustedApp", intent.getData() + " " + intent.getPackage());
            activity.startActivity(intent);
        }
    }

    public static void startActivityWithBlockedOrChooser(Activity activity, Intent intent) {
        if (TextUtils.equals(selectedPackageName, Prefs.BLOCKED_NAME)) {
            Toast.makeText(activity, R.string.blocked_url, Toast.LENGTH_SHORT).show();
        } else {
            intent.setComponent(null); // prompt user for Activity to view URI
            Log.i("startActivityWithBlockedOrChooser", intent.getData() + " " + intent.getPackage());
            activity.startActivity(intent);
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

    public static HttpURLConnection getHttpURLConnection(String urlString) throws IOException {
        return getHttpURLConnection(new URL(urlString));
    }

    public static HttpURLConnection getHttpURLConnection(URL url) throws IOException {
        HttpURLConnection connection = null;
        if (orbotHelper.isOrbotInstalled()) {
            InetSocketAddress isa = new InetSocketAddress("127.0.0.1", 8118);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, isa);
            connection = (HttpURLConnection) url.openConnection(proxy);
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        if (connection == null)
            return null;
        connection.setDoInput(true);
        connection.setDoOutput(false);
        connection.setInstanceFollowRedirects(false);
        return connection;
    }

    public static boolean requestOrbotStart(Activity activity) {
        if (orbotHelper.isOrbotInstalled()) {
            if (!orbotHelper.isOrbotRunning()) {
                Intent intent = new Intent(OrbotHelper.URI_ORBOT);
                intent.setAction(OrbotHelper.ACTION_START_TOR);
                activity.startActivityForResult(intent, ORBOT_START_RESULT);
                return true;
            }
        }
        return false;
    }
}

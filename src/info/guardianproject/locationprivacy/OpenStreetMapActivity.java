
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * This just serves as an optionally transparent filter for all OpenStreetMap
 * URIs to make sure that they are using HTTPS.
 */
public class OpenStreetMapActivity extends Activity {
    public static final String TAG = "OpenStreetMapActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        Log.i(TAG, "intent action " + intent.getAction());
        String categories = "";
        if (intent.getCategories() != null)
            for (String category : intent.getCategories())
                categories += " " + category;
        Log.i(TAG, "intent categories " + categories);

        Uri uri = intent.getData();
        Log.i(TAG, "uri: " + uri);
        if (uri != null) {
            Uri.Builder builder = uri.buildUpon();
            builder.scheme("https");
            builder.encodedAuthority("www.openstreetmap.org");
            intent.setData(builder.build());
        }

        Log.i(TAG, "startActivity uri: " + intent.getData());
        intent.setComponent(null); // prompt user for app to view new URI
        startActivity(intent);
        finish();
    }
}

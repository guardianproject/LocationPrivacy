
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class OsmAndActivity extends Activity {
    public static final String TAG = "OsmAndActivity";

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
            String lat = uri.getQueryParameter("lat");
            String lon = uri.getQueryParameter("lon");
            String zoom = uri.getQueryParameter("z");
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https");
            builder.encodedAuthority("www.openstreetmap.org");
            builder.encodedPath("/");
            builder.appendQueryParameter("mlat", lat);
            builder.appendQueryParameter("mlon", lon);
            builder.encodedFragment("map=" + zoom + "/" + lat + "/" + lon);
            intent.setData(builder.build());
        }

        Log.i(TAG, "startActivity uri: " + intent.getData());
        intent.setComponent(null); // prompt user for app to view new URI
        startActivity(intent);
        finish();
    }

}

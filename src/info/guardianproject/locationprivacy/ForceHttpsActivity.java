
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * This Activity is for location links that cannot be parsed at all, or
 * ultimately do not produce location information in a way that this app can get
 * it. So the best we can do is just force HTTPS. For example:
 * http://glympse.com/09PF-HK30
 *
 * @author hans
 */
public class ForceHttpsActivity extends Activity {
    public static final String TAG = "ForceHttpsActivity";

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
            intent.setData(builder.build());
        }
        App.startActivityWithBlockedOrChooser(this, intent);
        finish();
    }
}

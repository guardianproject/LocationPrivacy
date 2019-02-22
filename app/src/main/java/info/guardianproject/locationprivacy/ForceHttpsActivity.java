
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

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
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        Uri uri = intent.getData();
        if (uri != null) {
            Uri.Builder builder = uri.buildUpon();
            builder.scheme("https");
            intent.setData(builder.build());
        }
        App.startActivityWithBlockedOrChooser(this, intent);
        finish();
    }
}

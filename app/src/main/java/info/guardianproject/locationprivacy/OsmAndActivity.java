
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import net.osmand.util.GeoPointParserUtil;
import net.osmand.util.GeoPointParserUtil.GeoParsedPoint;

public class OsmAndActivity extends Activity {
    public static final String TAG = "OsmAndActivity";

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
            GeoParsedPoint point = GeoPointParserUtil.parse(uri.toString());
            if (point == null) {
                // can't do anything else, try to force HTTPS
                String host = uri.getHost();
                if (host.startsWith("maps.yandex.")
                        || host.equals("here.com")
                        || host.endsWith(".here.com")) {
                    Uri.Builder builder = uri.buildUpon();
                    builder.scheme("https");
                    intent.setData(builder.build());
                    App.startActivityWithBlockedOrChooser(this, intent);
                }
            } else {
                intent.setData(Uri.parse(point.getGeoUriString()));
                App.startActivityWithTrustedApp(this, intent);
            }
        }
        finish();
    }
}

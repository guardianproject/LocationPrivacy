
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import net.osmand.util.GeoPointParserUtil;
import net.osmand.util.MapUtils;
import net.osmand.util.GeoPointParserUtil.GeoParsedPoint;

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
            GeoParsedPoint point = GeoPointParserUtil.parse(uri.toString());
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https");
            builder.encodedAuthority("www.openstreetmap.org");
            builder.encodedPath("/");
            builder.appendQueryParameter("mlat", String.valueOf(point.getLatitude()));
            builder.appendQueryParameter("mlon", String.valueOf(point.getLongitude()));
            builder.encodedFragment("map=" + point.getZoom() + "/" + point.getLatitude() + "/"
                    + point.getLongitude());
            intent.setData(builder.build());
        }

        Log.i(TAG, "startActivity uri: " + intent.getData());
        intent.setComponent(null); // prompt user for app to view new URI
        startActivity(intent);
        finish();
    }

    public static Uri buildShortOsmUri(Uri uri) {
        return buildShortOsmUri(
                uri.getQueryParameter("lat"),
                uri.getQueryParameter("lon"),
                uri.getQueryParameter("z"));
    }

    public static Uri buildShortOsmUri(String lat, String lon, String z) {
        double latitude = 0;
        double longitude = 0;
        int zoom = 11;

        try {
            latitude = Double.parseDouble(lat);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            longitude = Double.parseDouble(lon);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            zoom = Integer.parseInt(z);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        String locString = MapUtils.createShortLinkString(latitude, longitude, zoom);
        return Uri.parse("https://openstreetmap.org/go/" + locString + "?m");
    }

}

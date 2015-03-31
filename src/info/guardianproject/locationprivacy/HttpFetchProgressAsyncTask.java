
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public abstract class HttpFetchProgressAsyncTask extends AsyncTask<Void, Void, String> {
    private static final String TAG = "HttpFetchProgressAsyncTask";
    private ProgressDialog dialog;
    final protected String urlString;

    public HttpFetchProgressAsyncTask(Activity activity, String urlString) {
        Log.v(TAG, "HttpFetchProgressAsyncTask " + activity.getClass().getSimpleName() + " "
                + urlString);
        this.dialog = new ProgressDialog(activity);
        this.urlString = urlString;
    }

    public HttpFetchProgressAsyncTask(Activity activity, Uri uri) {
        this(activity, uri.toString());
        Log.v(TAG, "HttpFetchProgressAsyncTask " + activity.getClass().getSimpleName() + " " + uri);
    }

    @Override
    protected void onPreExecute() {
        Log.v(TAG, "onPreExecute " + " " + urlString);
        dialog.setMessage("Fetching " + urlString);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setIndeterminate(true);
        dialog.show();
    }

    protected void dismissDialog() {
        Log.v(TAG, "dismissDialog " + " " + urlString);
        if (dialog != null) {
            if (dialog.isShowing())
                dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        Log.v(TAG, "onPostExecute " + " " + urlString + "  result: " + result);
        dismissDialog();
    }
}

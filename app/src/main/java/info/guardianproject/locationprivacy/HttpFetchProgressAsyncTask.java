
package info.guardianproject.locationprivacy;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;

public abstract class HttpFetchProgressAsyncTask extends AsyncTask<Void, Void, String> {

    private ProgressDialog dialog;
    final protected String urlString;

    public HttpFetchProgressAsyncTask(Activity activity, String urlString) {
        this.dialog = new ProgressDialog(activity);
        this.urlString = urlString;
    }

    public HttpFetchProgressAsyncTask(Activity activity, Uri uri) {
        this(activity, uri.toString());
    }

    @Override
    protected void onPreExecute() {
        dialog.setMessage("Fetching " + urlString);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setIndeterminate(true);
        dialog.show();
    }

    protected void dismissDialog() {
        if (dialog != null) {
            if (dialog.isShowing())
                dialog.dismiss();
            dialog = null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        dismissDialog();
    }
}

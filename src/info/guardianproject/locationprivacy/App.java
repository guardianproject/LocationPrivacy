
package info.guardianproject.locationprivacy;

import android.app.Application;

public class App extends Application {

    @Override
    public void onCreate() {
        Prefs.setup(this);
        super.onCreate();
    }
}

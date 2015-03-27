
package info.guardianproject.locationprivacy;

import android.text.TextUtils;

public class PackageMonitor extends AbstractPackageMonitor {

    @Override
    public void onPackageAdded(String packageName, int uid) {
        if (TextUtils.equals(packageName, App.OSMAND_FREE)
                || TextUtils.equals(packageName, App.OSMAND_PLUS)) {
            App.setSelectedPackageName(packageName);
            Prefs.setTrustedApp(packageName);
        }
    }

    @Override
    public void onPackageRemoved(String packageName, int uid) {
        /*
         * selectedPackageName and trustedApp might be out of sync if the
         * trustedApp is on External Storage, and it has been removed.
         */
        if (TextUtils.equals(packageName, App.getSelectedPackageName()))
            App.setSelectedPackageName(Prefs.CHOOSER_NAME);
        if (TextUtils.equals(packageName, Prefs.getTrustedApp()))
            Prefs.setTrustedApp(Prefs.CHOOSER_NAME);
    }

    @Override
    public void onPackagesAvailable(String[] packageNames) {
        // when the trusted app is again available, make sure it is in use
        String trustedPackageName = Prefs.getTrustedApp();
        for (String packageName : packageNames)
            if (TextUtils.equals(packageName, trustedPackageName))
                App.setSelectedPackageName(packageName);
    }

    @Override
    public void onPackagesUnavailable(String[] packageNames) {
        // if the trusted app is unavailable, temporarily use the Chooser
        String selectedPackageName = App.getSelectedPackageName();
        for (String packageName : packageNames)
            if (TextUtils.equals(packageName, selectedPackageName))
                App.setSelectedPackageName(Prefs.CHOOSER_NAME);
    }
}

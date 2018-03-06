package com.fonarik94;


import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class PreferencesManager {
    private Preferences prefs;
    //Preferences keys
    private static final String updatePeriod = "Update_period";
    private static final String workDir = "Work_Dir";
    private static final String servicePath = "Service_path";

    private static String defaultWorkDir = System.getenv("TEMP") + "\\img.jpg";

    public int getUpdatePeriod() {
        return prefs.getInt(updatePeriod, 30);
    }

    public String getWorkDirPath() {
        return prefs.get(workDir, defaultWorkDir);
    }

    public String getServicePath() {
        return prefs.get(servicePath, null);
    }

    public void setUpdatePeriod(int period) {
        prefs.putInt(updatePeriod, period);
    }

    public void setWorkDirPath(String path) {
        prefs.put(workDir, path);
    }

    public void setServicePath(String service) {
        prefs.put(servicePath, service);
    }

    public void resetAll() {
        try {
            prefs.clear();
        } catch (BackingStoreException bse) {
            System.out.println("Can't remove preferences. " + bse.getMessage());
        }
    }

    public PreferencesManager() {
        prefs = Preferences.userNodeForPackage(PreferencesManager.class);

    }

}

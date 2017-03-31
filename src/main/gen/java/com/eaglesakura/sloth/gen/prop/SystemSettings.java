package com.eaglesakura.sloth.gen.prop;


import com.eaglesakura.sloth.database.property.PropertyStore;
import com.eaglesakura.sloth.database.property.internal.GeneratedProperties;

public class SystemSettings extends GeneratedProperties {

    public static final String ID_LASTBOOTEDAPPVERSIONCODE = "SystemSettings.lastBootedAppVersionCode";
    public static final String ID_LASTBOOTEDAPPVERSIONNAME = "SystemSettings.lastBootedAppVersionName";
    public static final String ID_INSTALLUNIQUEID = "SystemSettings.installUniqueId";

    public SystemSettings() {
    }

    public SystemSettings(PropertyStore store) {
        setPropertyStore(store);
    }

    public void setLastBootedAppVersionCode(int set) {
        setProperty("SystemSettings.lastBootedAppVersionCode", set);
    }

    public int getLastBootedAppVersionCode() {
        return getIntProperty("SystemSettings.lastBootedAppVersionCode");
    }

    public void setLastBootedAppVersionName(String set) {
        setProperty("SystemSettings.lastBootedAppVersionName", set);
    }

    public String getLastBootedAppVersionName() {
        return getStringProperty("SystemSettings.lastBootedAppVersionName");
    }

    public void setInstallUniqueId(String set) {
        setProperty("SystemSettings.installUniqueId", set);
    }

    public String getInstallUniqueId() {
        return getStringProperty("SystemSettings.installUniqueId");
    }

}

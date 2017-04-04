package com.eaglesakura.sloth.gen;


class SystemSettings extends com.eaglesakura.sloth.db.property.internal.GeneratedProperties {
    
    static final String ID_LASTBOOTEDAPPVERSIONCODE = "SystemSettings.lastBootedAppVersionCode";
    static final String ID_LASTBOOTEDAPPVERSIONNAME = "SystemSettings.lastBootedAppVersionName";
    static final String ID_INSTALLUNIQUEID = "SystemSettings.installUniqueId";
    
    SystemSettings(){ }
    void setLastBootedAppVersionCode(int set){ setProperty("SystemSettings.lastBootedAppVersionCode", set); }
    int getLastBootedAppVersionCode(){ return getIntProperty("SystemSettings.lastBootedAppVersionCode"); }
    void setLastBootedAppVersionName(String set){ setProperty("SystemSettings.lastBootedAppVersionName", set); }
    String getLastBootedAppVersionName(){ return getStringProperty("SystemSettings.lastBootedAppVersionName"); }
    void setInstallUniqueId(String set){ setProperty("SystemSettings.installUniqueId", set); }
    String getInstallUniqueId(){ return getStringProperty("SystemSettings.installUniqueId"); }
    
}

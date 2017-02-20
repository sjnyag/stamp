package com.sjn.taggingplayer.db.dao;

import com.sjn.taggingplayer.db.Device;

import io.realm.Realm;

public class DeviceDao extends BaseDao {

    private static DeviceDao sInstance;

    public static DeviceDao getInstance() {
        if (sInstance == null) {
            sInstance = new DeviceDao();
        }
        return sInstance;
    }

    public Device findOrCreate(Realm realm, Device rawDevice) {
        Device device = realm.where(Device.class).equalTo("mModel", rawDevice.getModel()).equalTo("mOs", rawDevice.getOs()).findFirst();
        if (device == null) {
            device = realm.copyToRealm(rawDevice);
            device.setId(getAutoIncrementId(realm, Device.class));
        }
        return device;
    }

    public Device newStandalone() {
        return new Device();
    }

}

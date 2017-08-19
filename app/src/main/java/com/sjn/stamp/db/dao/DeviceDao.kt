package com.sjn.stamp.db.dao

import com.sjn.stamp.db.Device

import io.realm.Realm

object DeviceDao : BaseDao() {

    @Suppress("unused")
    fun findOrCreate(realm: Realm, rawDevice: Device): Device {
        var device: Device? = realm.where(Device::class.java).equalTo("model", rawDevice.model).equalTo("os", rawDevice.os).findFirst()
        if (device == null) {
            rawDevice.id = getAutoIncrementId(realm, Device::class.java)
            device = realm.copyToRealm(rawDevice)
        }
        return device!!
    }

    fun newStandalone(): Device = Device()

}

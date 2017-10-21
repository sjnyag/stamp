package com.sjn.stamp.db.dao

import com.sjn.stamp.db.Device
import io.realm.Realm

object DeviceDao : BaseDao() {

    fun findOrCreate(realm: Realm): Device {
        val device = Device()
        return findOrCreate(realm, device.model, device.os)
    }

    fun findOrCreate(realm: Realm, model: String, os: String): Device {
        var device: Device? = realm.where(Device::class.java).equalTo("model", model).equalTo("os", os).findFirst()
        if (device == null) {
            realm.beginTransaction()
            device = realm.createObject(Device::class.java, CategoryStampDao.getAutoIncrementId(realm, Device::class.java))
            device.model = model
            device.os = os
            realm.commitTransaction()
            return device
        }
        return device
    }

}

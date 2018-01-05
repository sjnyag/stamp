package com.sjn.stamp.model.dao

import io.realm.Realm
import io.realm.RealmModel
import java.lang.reflect.ParameterizedType

abstract class BaseDao<T : RealmModel> {

    fun getAutoIncrementId(realm: Realm): Long {
        val entityClass = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0]
        val maxId = realm.where(entityClass as Class<T>).max("id")
        return if (maxId != null) {
            maxId.toInt() + 1L
        } else 1L
    }
}

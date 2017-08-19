package com.sjn.stamp.db.dao

import com.sjn.stamp.constant.CategoryType
import com.sjn.stamp.db.CategoryStamp

import io.realm.Realm

object CategoryStampDao : BaseDao() {
    fun findCategoryStampList(realm: Realm, categoryType: CategoryType, categoryValue: String): List<CategoryStamp> =
            realm.where(CategoryStamp::class.java).equalTo("type", categoryType.databaseValue).equalTo("value", categoryValue).findAll()

    fun findAll(realm: Realm): List<CategoryStamp> =
            realm.where(CategoryStamp::class.java).findAll()

    fun remove(realm: Realm, name: String, categoryType: CategoryType, categoryValue: String, isSystem: Boolean) {
        realm.beginTransaction()
        realm.where(CategoryStamp::class.java).equalTo("name", name).equalTo("type", categoryType.databaseValue).equalTo("value", categoryValue).equalTo("isSystem", isSystem).findAll().deleteAllFromRealm()
        realm.commitTransaction()
    }

    fun remove(realm: Realm, name: String, isSystem: Boolean) {
        realm.beginTransaction()
        realm.where(CategoryStamp::class.java).equalTo("name", name).equalTo("isSystem", isSystem).findAll().deleteAllFromRealm()
        realm.commitTransaction()
    }

    fun save(realm: Realm, name: String?, categoryType: CategoryType, categoryValue: String, isSystem: Boolean) {
        if (name == null || name.isEmpty()) {
            return
        }
        realm.beginTransaction()
        var categoryStamp: CategoryStamp? = realm.where(CategoryStamp::class.java).equalTo("name", name).equalTo("type", categoryType.databaseValue).equalTo("value", categoryValue).findFirst()
        if (categoryStamp == null) {
            categoryStamp = realm.createObject(CategoryStamp::class.java, getAutoIncrementId(realm, CategoryStamp::class.java))
            categoryStamp.name = name
            categoryStamp.type = categoryType.databaseValue
            categoryStamp.isSystem = isSystem
            categoryStamp.value = categoryValue
        }
        realm.commitTransaction()
    }

    fun newStandalone(): CategoryStamp = CategoryStamp()

}

package com.sjn.stamp.model.dao

import com.sjn.stamp.model.constant.CategoryType
import com.sjn.stamp.model.CategoryStamp

import io.realm.Realm

object CategoryStampDao : BaseDao() {

    fun findAll(realm: Realm): List<CategoryStamp> =
            realm.where(CategoryStamp::class.java).findAll() ?: emptyList()

    fun findByName(realm: Realm, name: String, isSystem: Boolean): List<CategoryStamp> =
            realm.where(CategoryStamp::class.java).equalTo("name", name).equalTo("isSystem", isSystem).findAll() ?: emptyList()

    fun findCategoryStampList(realm: Realm, categoryType: CategoryType, categoryValue: String): List<CategoryStamp> =
            realm.where(CategoryStamp::class.java).equalTo("type", categoryType.databaseValue).equalTo("value", categoryValue).findAll() ?: emptyList()

    fun findOrCreate(realm: Realm, name: String, categoryType: CategoryType, categoryValue: String, isSystem: Boolean): CategoryStamp {
        var categoryStamp: CategoryStamp? = realm.where(CategoryStamp::class.java).equalTo("name", name).equalTo("type", categoryType.databaseValue).equalTo("value", categoryValue).equalTo("isSystem", isSystem).findFirst()
        if (categoryStamp == null) {
            realm.beginTransaction()
            categoryStamp = realm.createObject(CategoryStamp::class.java, getAutoIncrementId(realm, CategoryStamp::class.java))
            categoryStamp.name = name
            categoryStamp.type = categoryType.databaseValue
            categoryStamp.isSystem = isSystem
            categoryStamp.value = categoryValue
            realm.commitTransaction()
            return categoryStamp
        }
        return categoryStamp
    }

    fun create(realm: Realm, name: String?, categoryType: CategoryType, categoryValue: String, isSystem: Boolean) {
        if (name == null || name.isEmpty()) {
            return
        }
        findOrCreate(realm, name, categoryType, categoryValue, isSystem)
    }

    fun delete(realm: Realm, name: String, isSystem: Boolean) {
        realm.beginTransaction()
        realm.where(CategoryStamp::class.java).equalTo("name", name).equalTo("isSystem", isSystem).findAll().deleteAllFromRealm()
        realm.commitTransaction()
    }

    fun delete(realm: Realm, name: String, categoryType: CategoryType, categoryValue: String, isSystem: Boolean) {
        realm.beginTransaction()
        realm.where(CategoryStamp::class.java).equalTo("name", name).equalTo("type", categoryType.databaseValue).equalTo("value", categoryValue).equalTo("isSystem", isSystem).findAll().deleteAllFromRealm()
        realm.commitTransaction()
    }

}

package com.sjn.stamp.db.dao;

import com.sjn.stamp.constant.CategoryType;
import com.sjn.stamp.db.CategoryStamp;

import java.util.List;

import io.realm.Realm;

public class CategoryStampDao extends BaseDao {

    private static CategoryStampDao sInstance;

    public static CategoryStampDao getInstance() {
        if (sInstance == null) {
            sInstance = new CategoryStampDao();
        }
        return sInstance;
    }

    public List<CategoryStamp> findAllStampGroupByName(Realm realm) {
        return realm.where(CategoryStamp.class).distinct("name");
    }

    public List<CategoryStamp> findCategoryStampList(Realm realm, CategoryType categoryType, String categoryValue) {
        return realm.where(CategoryStamp.class).equalTo("type", categoryType.getValue()).equalTo("value", categoryValue).findAll();
    }

    public List<CategoryStamp> findCategoryStampList(Realm realm, String stampName) {
        return realm.where(CategoryStamp.class).equalTo("name", stampName).findAll();
    }

    public List<CategoryStamp> findAll(Realm realm) {
        return realm.where(CategoryStamp.class).findAll();
    }

    public void remove(Realm realm, String name, CategoryType categoryType, String categoryValue) {
        realm.beginTransaction();
        realm.where(CategoryStamp.class).equalTo("name", name).equalTo("type", categoryType.getValue()).equalTo("value", categoryValue).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public void remove(Realm realm, final String name) {
        realm.beginTransaction();
        realm.where(CategoryStamp.class).equalTo("name", name).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public void save(Realm realm, String name, CategoryType categoryType, String categoryValue) {
        if (name == null || name.isEmpty()) {
            return;
        }
        realm.beginTransaction();
        CategoryStamp categoryStamp = realm.where(CategoryStamp.class).equalTo("name", name).equalTo("type", categoryType.getValue()).equalTo("value", categoryValue).findFirst();
        if (categoryStamp == null) {
            categoryStamp = realm.createObject(CategoryStamp.class, getAutoIncrementId(realm, CategoryStamp.class));
            categoryStamp.setName(name);
            categoryStamp.setType(categoryType.getValue());
            categoryStamp.setValue(categoryValue);
        }
        realm.commitTransaction();
    }

    public CategoryStamp newStandalone() {
        return new CategoryStamp();
    }

}

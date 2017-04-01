package com.sjn.taggingplayer.db.dao;

import com.sjn.taggingplayer.constant.CategoryType;
import com.sjn.taggingplayer.db.CategoryTag;
import com.sjn.taggingplayer.db.SongTag;

import java.util.List;

import io.realm.Realm;

public class CategoryTagDao extends BaseDao {

    private static CategoryTagDao sInstance;

    public static CategoryTagDao getInstance() {
        if (sInstance == null) {
            sInstance = new CategoryTagDao();
        }
        return sInstance;
    }

    public List<CategoryTag> findAllTagGroupByName(Realm realm) {
        return realm.where(CategoryTag.class).distinct("mName");
    }

    public List<CategoryTag> findCategoryTagList(Realm realm, CategoryType categoryType, String categoryValue) {
        return realm.where(CategoryTag.class).equalTo("mType", categoryType.getValue()).equalTo("mValue", categoryValue).findAll();
    }

    public List<CategoryTag> findCategoryTagList(Realm realm, String tagName) {
        return realm.where(CategoryTag.class).equalTo("mName", tagName).findAll();
    }

    public List<CategoryTag> findAll(Realm realm) {
        return realm.where(CategoryTag.class).findAll();
    }

    public void remove(Realm realm, String name, CategoryType categoryType, String categoryValue) {
        realm.beginTransaction();
        realm.where(CategoryTag.class).equalTo("mName", name).equalTo("mType", categoryType.getValue()).equalTo("mValue", categoryValue).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public void remove(Realm realm, final String name) {
        realm.beginTransaction();
        realm.where(CategoryTag.class).equalTo("mName", name).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }

    public void save(Realm realm, String name, CategoryType categoryType, String categoryValue) {
        if (name == null || name.isEmpty()) {
            return;
        }
        realm.beginTransaction();
        CategoryTag categoryTag = realm.where(CategoryTag.class).equalTo("mName", name).equalTo("mType", categoryType.getValue()).equalTo("mValue", categoryValue).findFirst();
        if (categoryTag == null) {
            categoryTag = realm.createObject(CategoryTag.class);
            categoryTag.setId(getAutoIncrementId(realm, CategoryTag.class));
            categoryTag.setName(name);
            categoryTag.setType(categoryType.getValue());
            categoryTag.setValue(categoryValue);
        }
        realm.commitTransaction();
    }

    public CategoryTag newStandalone() {
        return new CategoryTag();
    }

}

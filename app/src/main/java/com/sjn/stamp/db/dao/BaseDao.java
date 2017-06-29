package com.sjn.stamp.db.dao;

import io.realm.Realm;

public abstract class BaseDao {

    protected Integer getAutoIncrementId(Realm realm, Class clazz) {
        Number maxId = realm.where(clazz).max("mId");
        if (maxId != null) {
            return maxId.intValue() + 1;
        }
        return 1;
    }
}

package com.sjn.stamp.db.dao;

import io.realm.Realm;

public abstract class BaseDao {

    protected Long getAutoIncrementId(Realm realm, Class clazz) {
        Number maxId = realm.where(clazz).max("id");
        if (maxId != null) {
            return maxId.intValue() + 1L;
        }
        return 1L;
    }
}

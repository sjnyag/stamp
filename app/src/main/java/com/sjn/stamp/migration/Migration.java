package com.sjn.stamp.migration;

import io.realm.DynamicRealm;
import io.realm.DynamicRealmObject;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class Migration implements RealmMigration {
    @Override
    public void migrate(final DynamicRealm realm, long oldVersion, long newVersion) {

        RealmSchema schema = realm.getSchema();
        if (oldVersion == 0) {
            RealmObjectSchema artistSchema = schema.create("Artist")
                    .addField("mId", long.class, FieldAttribute.PRIMARY_KEY)
                    .addField("mName", String.class, FieldAttribute.INDEXED)
                    .addField("mAlbumArtUri", String.class);

            RealmObjectSchema songSchema = schema.get("Song");

            songSchema.addRealmObjectField("mArtist_new", artistSchema)
                    .transform(new RealmObjectSchema.Function() {
                        @Override
                        public void apply(DynamicRealmObject obj) {
                            obj.set("mArtist_new", findOrCreateArtist(obj.getString("mArtist"), obj.getString("mAlbumArtUri")));
                        }

                        DynamicRealmObject findOrCreateArtist(String name, String artUrl) {
                            DynamicRealmObject artist = realm.where("Artist").equalTo("mName", name).findFirst();
                            if (artist == null) {
                                artist = realm.createObject("Artist", getAutoIncrementId(realm, "Artist"));
                                artist.setString("mName", name);
                                artist.setString("mAlbumArtUri", artUrl);
                            }
                            return artist;
                        }

                        Integer getAutoIncrementId(DynamicRealm realm, String clazz) {
                            Number maxId = realm.where(clazz).max("mId");
                            if (maxId != null) {
                                return maxId.intValue() + 1;
                            }
                            return 1;
                        }
                    })
                    .removeField("mArtist")
                    .renameField("mArtist_new", "mArtist");
            oldVersion++;
        }
    }
}
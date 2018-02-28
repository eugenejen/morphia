package org.mongodb.morphia.utils;


import org.junit.Test;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.TestBase;

import static org.junit.Assert.assertEquals;

public class LongIdEntityTest extends TestBase {
    @Test
    public void testMonoIncreasingId() throws Exception {
        MyEntity ent = new MyEntity(getDatastore());
        getDatastore().save(ent);
        assertEquals(1L, ent.getMyLongId(), 0);
        ent = new MyEntity(getDatastore());
        getDatastore().save(ent);
        assertEquals(2L, ent.getMyLongId(), 0);
    }

    static class MyEntity extends LongIdEntity {
        protected MyEntity() {
            super(null);
        }

        MyEntity(final Datastore ds) {
            super(ds);
        }
    }

}

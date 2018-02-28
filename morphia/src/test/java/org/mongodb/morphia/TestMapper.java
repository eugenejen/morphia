package org.mongodb.morphia;


import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.PostLoad;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.mapping.EmbeddedMappingTest.AnotherNested;
import org.mongodb.morphia.mapping.EmbeddedMappingTest.Nested;
import org.mongodb.morphia.mapping.EmbeddedMappingTest.NestedImpl;
import org.mongodb.morphia.mapping.MappedClass;
import org.mongodb.morphia.mapping.Mapper;

import java.io.Serializable;
import java.util.List;

import static java.util.Arrays.asList;


/**
 * Tests mapper functions; this is tied to some of the internals.
 */
public class TestMapper extends TestBase {
    @Test
    public void serializableId() {
        final CustomId cId = new CustomId();
        cId.id = new ObjectId();
        cId.type = "banker";

        final UsesCustomIdObject object = new UsesCustomIdObject();
        object.id = cId;
        object.text = "hllo";
        getDatastore().save(object);
    }

    @Test
    public void singleLookup() {
        A.loadCount = 0;
        final A a = new A();
        HoldsMultipleA holder = new HoldsMultipleA();
        holder.a1 = a;
        holder.a2 = a;
        getDatastore().save(asList(a, holder));
        holder = getDatastore().get(HoldsMultipleA.class, holder.id);
        Assert.assertEquals(1, A.loadCount);
        Assert.assertTrue(holder.a1 == holder.a2);
    }

    @Test
    public void subTypes() {
        getMorphia().map(NestedImpl.class, AnotherNested.class);

        Mapper mapper = getMorphia().getMapper();
        List<MappedClass> subTypes = mapper.getSubTypes(mapper.getMappedClass(Nested.class));
        Assert.assertTrue(subTypes.contains(mapper.getMappedClass(NestedImpl.class)));
        Assert.assertTrue(subTypes.contains(mapper.getMappedClass(AnotherNested.class)));
    }

    public static class A {
        private static int loadCount;
        @Id
        private ObjectId id;

        @PostLoad
        protected void postConstruct() {
            if (loadCount > 1) {
                throw new RuntimeException();
            }

            loadCount++;
        }

        String getId() {
            return id.toString();
        }
    }

    @Entity("holders")
    public static class HoldsMultipleA {
        @Id
        private ObjectId id;
        @Reference
        private A a1;
        @Reference
        private A a2;
    }

    public static class CustomId implements Serializable {

        @Property("v")
        private ObjectId id;
        @Property("t")
        private String type;

        public ObjectId getId() {
            return id;
        }

        public void setId(final ObjectId id) {
            this.id = id;
        }

        public String getType() {
            return type;
        }

        public void setType(final String type) {
            this.type = type;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((id == null) ? 0 : id.hashCode());
            result = prime * result + ((type == null) ? 0 : type.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof CustomId)) {
                return false;
            }
            final CustomId other = (CustomId) obj;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            if (type == null) {
                if (other.type != null) {
                    return false;
                }
            } else if (!type.equals(other.type)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("CustomId [");
            if (id != null) {
                builder.append("id=").append(id).append(", ");
            }
            if (type != null) {
                builder.append("type=").append(type);
            }
            builder.append("]");
            return builder.toString();
        }
    }

    public static class UsesCustomIdObject {
        @Id
        private CustomId id;
        private String text;

        public CustomId getId() {
            return id;
        }

        public void setId(final CustomId id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(final String text) {
            this.text = text;
        }
    }

}

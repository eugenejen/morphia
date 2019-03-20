package dev.morphia.mapping.experimental;

import com.mongodb.DBObject;
import com.mongodb.DBRef;
import com.mongodb.client.MongoCursor;
import dev.morphia.Datastore;
import dev.morphia.mapping.MappedClass;
import dev.morphia.mapping.MappedField;
import dev.morphia.mapping.Mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @param <T>
 * @morphia.internal
 */
public class MapReference<T> extends MorphiaReference<Map<String, T>> {
    private Map<String, Object> ids;
    private Map<String, T> values;
    private Map<String, List<Object>> collections = new HashMap<String, List<Object>>();

    /**
     * @morphia.internal
     */
    public MapReference(final Datastore datastore, final MappedClass mappedClass, final String collection, final Map<String, Object> ids) {
        super(datastore, mappedClass);
        this.ids = unwrap(collection, ids);
    }

    protected Map<String, Object> unwrap(final String collection, final Map<String, Object> ids) {
        Map<String, Object> unwrapped = ids;
        if(ids != null) {
            for (final Entry<String, Object> entry : ids.entrySet()) {
                CollectionReference.collate(collections, collection, entry.getValue());
            }
        }

        return unwrapped;
    }

    protected MapReference(final Map<String, T> values) {
        set(values);
    }

    public Map<String, T> get() {
        if (values == null && ids != null) {
            values = new LinkedHashMap<String, T>();
            mergeReads();
        }
        return values;
    }

    private void mergeReads() {
        for (final Entry<String, List<Object>> entry : collections.entrySet()) {
            readFromSingleCollection(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    public void readFromSingleCollection(final String collection, final List<Object> collectionIds) {

        final Class<?> collectionType = getDatastore().getMapper().getClassFromCollection(collection);
        final MongoCursor<T> cursor = (MongoCursor<T>) getDatastore().find(collectionType)
                                                                     .filter("_id in ", collectionIds)
                                                                     .find();
        try {
            final Map<Object, T> idMap = new HashMap<Object, T>();
            while (cursor.hasNext()) {
                final T entity = cursor.next();
                idMap.put(getDatastore().getMapper().getId(entity), entity);
            }

            for (final Entry<String, Object> entry : ids.entrySet()) {
                final Object id = entry.getValue();
                final T value = idMap.get(id instanceof DBRef ? ((DBRef)id).getId() : id);
                if(value != null) {
                    values.put(entry.getKey(), value);
                }
            }
        } finally {
            cursor.close();
        }
    }

    public void set(Map<String, T> values) {
        this.values = values;
    }

    public boolean isResolved() {
        return values != null;
    }

    @Override
    public Object encode(final Mapper mapper, final Object value, final MappedField field) {
        if (isResolved()) {
            Map<String, Object> ids = new LinkedHashMap<String, Object>();
            for (final Entry<String, T> entry : get().entrySet()) {
                ids.put(entry.getKey(), wrapId(mapper, field, entry.getValue()));
            }
            return ids;
        } else {
            return null;
        }
    }

    public static MapReference decode(final Datastore datastore, final Mapper mapper, final MappedField mappedField,
                                      final DBObject dbObject) {
        final Class subType = mappedField.getTypeParameters().get(0).getSubClass();

        final Map<String, Object> ids = (Map<String, Object>) mappedField.getDbObjectValue(dbObject);
        MapReference reference = null;
        if (ids != null) {
            final Collection<Object> values = ids.values();
            final Object first = values.iterator().next();
            String collection = null;
            if (first instanceof DBRef) {
                collection = ((DBRef) first).getCollectionName();
            }

            reference = new MapReference(datastore, mapper.getMappedClass(subType), collection, ids);
        }

        return reference;
    }
}

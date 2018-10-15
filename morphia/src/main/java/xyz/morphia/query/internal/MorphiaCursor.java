package xyz.morphia.query.internal;


import com.mongodb.Cursor;
import com.mongodb.DBObject;
import com.mongodb.ServerAddress;
import com.mongodb.ServerCursor;
import com.mongodb.client.MongoCursor;
import xyz.morphia.Datastore;
import xyz.morphia.mapping.Mapper;
import xyz.morphia.mapping.cache.EntityCache;

import java.util.NoSuchElementException;


/**
 * @param <T> the original type being iterated
 */
public class MorphiaCursor<T> implements MongoCursor<T> {
    private final Cursor wrapped;
    private final Mapper mapper;
    private final Class<T> clazz;
    private final EntityCache cache;
    private final Datastore datastore;

    /**
     * Creates a MorphiaCursor
     *
     * @param datastore  the Datastore to use when fetching this reference
     * @param cursor     the Iterator to use
     * @param mapper     the Mapper to use
     * @param clazz      the original type being iterated
     * @param cache      the EntityCache
     */
    public MorphiaCursor(final Datastore datastore, final Cursor cursor, final Mapper mapper, final Class<T> clazz,
                         final EntityCache cache) {
        wrapped = cursor;
        this.mapper = mapper;
        this.clazz = clazz;
        this.cache = cache;
        this.datastore = datastore;
    }

    /**
     * Closes the underlying cursor.
     */
    public void close() {
        if (wrapped != null) {
            wrapped.close();
        }
    }

    @Override
    public boolean hasNext() {
        if (wrapped == null) {
            return false;
        }
        return wrapped.hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return mapper.fromDBObject(datastore, clazz, wrapped.next(), cache);
    }

    @Override
    public T tryNext() {
        if (hasNext()) {
            return next();
        } else {
            return null;
        }
    }

    @Override
    public ServerCursor getServerCursor() {
        return new ServerCursor(wrapped.getCursorId(), wrapped.getServerAddress());
    }

    @Override
    public ServerAddress getServerAddress() {
        return wrapped.getServerAddress();
    }

    @Override
    public void remove() {
        wrapped.remove();
    }

    protected DBObject getNext() {
        return wrapped.next();
    }
}

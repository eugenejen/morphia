package xyz.morphia.query;


import com.mongodb.Bytes;
import com.mongodb.client.MongoCursor;
import xyz.morphia.Key;

import java.util.List;

/**
 * The results of a query.  These results aren't materialized until a method on this interface is called.
 *
 * @param <T>
 */
public interface QueryResults<T> extends Iterable<T> {
    /**
     * Execute the query and get the results (as a {@code List<Key<T>>})  This method is provided as a convenience;
     *
     * @return returns a List of the keys of the documents returned by a query
     * @deprecated use {@link #keys()}
     */
    @Deprecated
    List<Key<T>> asKeyList();

    /**
     * Execute the query and get the results (as a {@code List<Key<T>>})  This method is provided as a convenience;
     *
     * @param options the options to apply to the find operation
     * @return returns a List of the keys of the documents returned by a query
     * @since 1.3
     * @deprecated use {@link #keys(FindOptions)}
     */
    @Deprecated
    List<Key<T>> asKeyList(FindOptions options);

    /**
     * Execute the query and get the results (as a {@code MongoCursor<Key<T>>})
     *
     * @return the keys of the documents returned by this query
     */
    MongoCursor<Key<T>> keys();

    /**
     * Execute the query and get the results (as a {@code MongoCursor<Key<T>>})
     *
     * @param options the options to apply to the find operation
     * @return the keys of the documents returned by this query
     * @since 1.4
     */
    MongoCursor<Key<T>> keys(FindOptions options);

    /**
     * Execute the query and get the results.
     *
     * @return returns a List of the documents returned by a query
     * @deprecated use {@link #find(FindOptions)}
     */
    @Deprecated
    List<T> asList();

    /**
     * Execute the query and get the results.
     *
     * @param options the options to apply to the find operation
     * @return returns a List of the documents returned by a query
     * @since 1.3
     * @deprecated use {@link #find(FindOptions)}
     */
    @Deprecated
    List<T> asList(FindOptions options);

    /**
     * Count the total number of values in the result, ignoring limit and offset
     *
     * @return the count
     * @deprecated use {@link #count()} instead
     */
    @Deprecated
    long countAll();

    /**
     * Count the total number of values in the result, ignoring limit and offset
     *
     * @return the count
     * @since 1.3
     */
    long count();

    /**
     * Count the total number of values in the result, ignoring limit and offset
     *
     * @param options the options to apply to the count operation
     * @return the count
     * @since 1.3
     */
    long count(CountOptions options);

    /**
     * Execute the query and get the results.  This method is provided for orthogonality; Query.fetch().iterator() is identical to
     * Query.iterator().
     *
     * @return an Iterator of the results
     * @deprecated use {@link #find(FindOptions)} instead
     */
    @Deprecated
    MorphiaIterator<T, T> fetch();

    /**
     * Execute the query and get the results.  This method is provided for orthogonality; Query.fetch().iterator() is identical to
     * Query.iterator().
     *
     * @param options the options to apply to the find operation
     * @return an Iterator of the results
     * @since 1.3
     * @deprecated use {@link #find(FindOptions)} instead
     */
    @Deprecated
    MorphiaIterator<T, T> fetch(FindOptions options);

    /**
     * Execute the query and get the results.  This method is provided for orthogonality; {@link Query#find()#iterator()} is identical to
     * {@link Query#iterator()}.
     *
     * @return an Iterator of the results
     * @since 1.4
     */
    MongoCursor<T> find();

    /**
     * Execute the query and get the results.  This method is provided for orthogonality; Query.find().iterator() is identical to
     * Query.iterator().
     *
     * @param options the options to apply to the find operation
     * @return an Iterator of the results
     * @since 1.4
     */
    MongoCursor<T> find(FindOptions options);

    /**
     * Execute the query and get only the ids of the results.  This is more efficient than fetching the actual results (transfers less
     * data).
     *
     * @return an Iterator of the empty entities
     * @deprecated use {@link #keys()} instead
     */
    @Deprecated
    MorphiaIterator<T, T> fetchEmptyEntities();

    /**
     * Execute the query and get only the ids of the results.  This is more efficient than fetching the actual results (transfers less
     * data).
     *
     * @param options the options to apply to the find operation
     * @return an Iterator of the empty entities
     * @since 1.3
     * @deprecated use {@link #keys(FindOptions)} instead
     */
    @Deprecated
    MorphiaIterator<T, T> fetchEmptyEntities(FindOptions options);

    /**
     * Execute the query and get the keys for the objects.
     *
     * @return the Key Iterator
     * @see #fetchEmptyEntities
     * @deprecated use {@link #keys()}
     */
    @Deprecated
    MorphiaKeyIterator<T> fetchKeys();

    /**
     * Execute the query and get the keys for the objects.
     *
     * @param options the options to apply to the find operation
     * @return the Key Iterator
     * @since 1.3
     * @deprecated use {@link #keys(FindOptions)}
     */
    @Deprecated
    MorphiaKeyIterator<T> fetchKeys(FindOptions options);

    /**
     * Gets the first entity in the result set.  Obeys the {@link Query} offset value.
     *
     * @return the only instance in the result, or null if the result set is empty.
     * @deprecated use {@link #find()} and {@link MongoCursor#hasNext()} or {@link MongoCursor#tryNext()}
     */
    @Deprecated
    T get();

    /**
     * Gets the first entity in the result set.  Obeys the {@link Query} offset value.
     *
     * @param options the options to apply to the find operation
     * @return the only instance in the result, or null if the result set is empty.
     * @since 1.3
     * @deprecated use {@link #find()} and {@link MongoCursor#hasNext()} or {@link MongoCursor#tryNext()}
     */
    @Deprecated
    T get(FindOptions options);

    /**
     * Get the key of the first entity in the result set.  Obeys the {@link Query} offset value.
     *
     * @return the key of the first instance in the result, or null if the result set is empty.
     * @deprecated use {@link #find()} and {@link MongoCursor#hasNext()} or {@link MongoCursor#tryNext()}
     */
    @Deprecated
    Key<T> getKey();

    /**
     * Get the key of the first entity in the result set.  Obeys the {@link Query} offset value.
     *
     * @param options the options to apply to the find operation
     * @return the key of the first instance in the result, or null if the result set is empty.
     * @since 1.3
     * @deprecated use {@link #find(FindOptions)} and {@link MongoCursor#hasNext()} or {@link MongoCursor#tryNext()}
     */
    @Deprecated
    Key<T> getKey(FindOptions options);

    /**
     * Calls {@code tail(true);}
     *
     * @return an Iterator.
     * @see #tail(boolean)
     * @deprecated set the CursorType on {@link FindOptions} and use {@link #find(FindOptions)} instead
     */
    @Deprecated
    MorphiaIterator<T, T> tail();

    /**
     * Returns an tailing iterator over a set of elements of type T. If awaitData is true, this iterator blocks on hasNext() until new data
     * is avail (or some amount of time has passed). Note that if no data is available at all, hasNext() might return immediately. You
     * should wrap tail calls in a loop if you want this to be blocking.
     *
     * @param awaitData passes the awaitData to the cursor
     * @return an Iterator.
     * @see Bytes#QUERYOPTION_AWAITDATA
     * @deprecated set the CursorType on {@link FindOptions}  and use {@link #find(FindOptions)} instead. This can be replicated using
     * {@code findOptions.cursorType (awaitData ? TailableAwait : Tailable)}
     */
    @Deprecated
    MorphiaIterator<T, T> tail(boolean awaitData);
}

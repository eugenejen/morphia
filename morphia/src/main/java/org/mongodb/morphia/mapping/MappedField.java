/*
 * Copyright 2008-2016 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.morphia.mapping;


import com.mongodb.DBRef;
import org.bson.codecs.pojo.PropertyModel;
import org.bson.codecs.pojo.TypeData;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Property;
import org.mongodb.morphia.annotations.Reference;
import org.mongodb.morphia.annotations.Serialized;
import org.mongodb.morphia.annotations.Transient;
import org.mongodb.morphia.annotations.Version;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.groupingBy;


/**
 * Represents the mapping of this field to/from mongodb (name, list<annotation>)
 */
@SuppressWarnings("unchecked")
public class MappedField {
    private final Map<Class<? extends Annotation>, List<Annotation>> annotations;
    private final List<MappedField> typeParameters = new ArrayList<>();
    private MappedClass declaringClass;
    private PropertyModel property; // the field :)
    private Class realType; // the real type
    private Class specializedType; // the type (T) for the Collection<T>/T[]/Map<?,T>
    private Type mapKeyType; // the type (T) for the Map<T,?>
    private boolean isSingleValue = true; // indicates the field is a single value
    // indicated the type is a mongo compatible type (our version of value-type)
    private boolean isMap; // indicated if it implements Map interface
    private boolean isSet; // indicated if the collection is a set
    //for debugging
    private boolean isArray; // indicated if it is an Array
    private boolean isCollection; // indicated if the collection is a list)
    private TypeData<?> typeData;

    MappedField(final MappedClass declaringClass, final PropertyModel f) {
        this.declaringClass = declaringClass;
        property = f;
        typeData = property.getTypeData();
        realType = typeData.getType();

        annotations = Arrays.stream(realType.getAnnotations())
                            .collect(groupingBy(
                                annotation -> (Class<? extends Annotation>) annotation.getClass()));
        discoverMultivalued();
    }

    /**
     * @param clazz the annotation to search for
     * @param <T>   the type of the annotation
     * @return the annotation instance if it exists on this field
     */
    @SuppressWarnings("unchecked")
    public <T extends Annotation> T getAnnotation(final Class<T> clazz) {
        return (T) annotations.get(clazz);
    }

    /**
     * @return the concrete type of the MappedField
     */
    public Class getConcreteType() {
        final Embedded e = getAnnotation(Embedded.class);
        if (e != null) {
            final Class concrete = e.concreteClass();
            if (concrete != Object.class) {
                return concrete;
            }
        }

        final Property p = getAnnotation(Property.class);
        if (p != null) {
            final Class concrete = p.concreteClass();
            if (concrete != Object.class) {
                return concrete;
            }
        }
        return getType();
    }

    /**
     * @return the declaring class of the java field
     */
    public Class getDeclaringClass() {
        return declaringClass.getClazz();
    }

    /**
     * Gets the value of the field mapped on the instance given.
     *
     * @param instance the instance to use
     * @return the value stored in the java field
     */
    public Object getFieldValue(final Object instance) {
        return property.getPropertyAccessor().get(instance);
    }

    /**
     * @return the name of the java field, as declared on the class
     */
    public String getJavaFieldName() {
        return property.getName();
    }

    /**
     * @return the name of the field's (key)name for mongodb
     */
    public String getNameToStore() {
        return getMappedFieldName();
    }

    /**
     * If the java field is a list/array/map then the sub-type T is returned (ex. List<T>, T[], Map<?,T>
     *
     * @return the parameterized type of the field
     */
    public Class getSpecializedType() {
        return specializedType;
    }

    /**
     * @return true if this field is marked as serializable
     */
    public boolean isSerializable() {
        return hasAnnotation(Serialized.class)
               && Serializable.class.isAssignableFrom(getType());

    }

    /**
     * @return true if this field is marked as transient
     */
    public boolean isTransient() {
        return !hasAnnotation(Transient.class)
               && !hasAnnotation(java.beans.Transient.class)
               && Modifier.isTransient(getType().getModifiers());
    }

    /**
     * @return the type of the underlying java field
     */
    public Class getType() {
        return realType;
    }

    /**
     * @return the type parameters defined on the field
     */
    public List<MappedField> getTypeParameters() {
        return typeParameters;
    }

    /**
     * Indicates whether the annotation is present in the mapping (does not check the java field annotations, just the ones discovered)
     *
     * @param ann the annotation to search for
     * @return true if the annotation was found
     */
    public boolean hasAnnotation(final Class ann) {
        return annotations.containsKey(ann);
    }

    /**
     * @return true if the MappedField is an array
     */
    public boolean isArray() {
        return isArray;
    }

    /**
     * @return true if the MappedField is a Map
     */
    public boolean isMap() {
        return isMap;
    }

    /**
     * @return true if this field is a container type such as a List, Map, Set, or array
     */
    public boolean isMultipleValues() {
        return !isSingleValue();
    }

    /**
     * @return true if this field is a reference to a foreign document
     * @see Reference
     * @see Key
     * @see DBRef
     */
    public boolean isReference() {
        return hasAnnotation(Reference.class) || Key.class == getConcreteType() || DBRef.class == getConcreteType();
    }

    /**
     * @return true if the MappedField is a Set
     */
    public boolean isSet() {
        return isSet;
    }

    /**
     * @return true if this field is not a container type such as a List, Map, Set, or array
     */
    public boolean isSingleValue() {
        // TODO:  This is almost certainly broken.  revisit when things compile.
        return true;
    }

    /**
     * Sets the value for the java field
     *
     * @param instance the instance to update
     * @param value    the value to set
     */
    public void setFieldValue(final Object instance, final Object value) {
        property.getPropertyAccessor().set(instance, value);
    }

    @Override
    public String toString() {
        return getNameToStore();
    }

    /**
     * @return the name of the field's key-name for mongodb
     */
    private String getMappedFieldName() {
        if (hasAnnotation(Id.class)) {
            return Mapper.ID_KEY;
        } else if (hasAnnotation(Property.class)) {
            final Property mv = (Property) annotations.get(Property.class);
            if (!mv.value().equals(Mapper.IGNORED_FIELDNAME)) {
                return mv.value();
            }
        } else if (hasAnnotation(Reference.class)) {
            final Reference mr = (Reference) annotations.get(Reference.class);
            if (!mr.value().equals(Mapper.IGNORED_FIELDNAME)) {
                return mr.value();
            }
        } else if (hasAnnotation(Embedded.class)) {
            final Embedded me = (Embedded) annotations.get(Embedded.class);
            if (!me.value().equals(Mapper.IGNORED_FIELDNAME)) {
                return me.value();
            }
        } else if (hasAnnotation(Serialized.class)) {
            final Serialized me = (Serialized) annotations.get(Serialized.class);
            if (!me.value().equals(Mapper.IGNORED_FIELDNAME)) {
                return me.value();
            }
        } else if (hasAnnotation(Version.class)) {
            final Version me = (Version) annotations.get(Version.class);
            if (!me.value().equals(Mapper.IGNORED_FIELDNAME)) {
                return me.value();
            }
        }

        return property.getName();
    }

    private void discoverMultivalued() {
        if (realType.isArray()
            || Collection.class.isAssignableFrom(realType)
            || Map.class.isAssignableFrom(realType)
            || GenericArrayType.class.isAssignableFrom(realType.getClass())) {

            isSingleValue = false;

            isMap = Map.class.isAssignableFrom(realType);
            isSet = Set.class.isAssignableFrom(realType);
            isCollection = Collection.class.isAssignableFrom(realType);
            isArray = realType.isArray();

            // get the specializedType T, T[]/List<T>/Map<?,T>; subtype of Long[], List<Long> is Long
            specializedType = (realType.isArray()) ? realType.getComponentType() : typeData.getTypeParameters().get(0).getType();

            if (isMap) {
                mapKeyType = typeData.getTypeParameters().get(0).getType();
            }
        }
    }

    public TypeData<?> getTypeData() {
        return typeData;
    }
}

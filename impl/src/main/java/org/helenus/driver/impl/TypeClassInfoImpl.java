/*
 * Copyright (C) 2015-2016 The Helenus Driver Project Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.helenus.driver.impl;

import java.lang.reflect.Modifier;

import java.util.Map;
import java.util.Objects;

import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.Row;

import org.helenus.commons.lang3.reflect.ReflectionUtils;
import org.helenus.driver.ObjectConversionException;
import org.helenus.driver.info.TypeClassInfo;
import org.helenus.driver.persistence.Keyspace;
import org.helenus.driver.persistence.RootEntity;
import org.helenus.driver.persistence.Table;
import org.helenus.driver.persistence.TypeEntity;

/**
 * The <code>TypeClassInfoImpl</code> class provides information about a
 * particular POJO class.
 *
 * @copyright 2015-2016 The Helenus Driver Project Authors
 *
 * @author  The Helenus Driver Project Authors
 * @version 1 - Jan 19, 2015 - paouelle - Creation
 *
 * @param <T> The type of POJO represented by this class
 *
 * @since 2.0
 */
public class TypeClassInfoImpl<T>
  extends ClassInfoImpl<T>
  implements TypeClassInfo<T> {
  /**
   * Holds the class info for the root entity this POJO is a type.
   *
   * @author paouelle
   */
  private final RootClassInfoImpl<? super T> rinfo;

  /**
   * Holds the type for this POJO class.
   *
   * @author paouelle
   */
  private final String type;

  /**
   * Holds a flag indicating if this type was known to the root entity via the
   * @RootEntity annotation or if it was dynamically added later.
   *
   * @author paouelle
   */
  private final boolean dynamic;

  /**
   * Instantiates a new <code>TypeClassInfoImpl</code> object.
   *
   * @author paouelle
   *
   * @param  mgr the non-<code>null</code> statement manager
   * @param  rinfo the class info for the root entity this POJO is a type
   * @param  clazz the class of POJO for which to get a class info object for
   * @param  dynamic <code>true</code> if this type is dynamically being added
   *         to the root; <code>false</code> if it was known to the root via
   *         the @RootEnitty annotation
   * @throws NullPointerException if <code>rinfo</code> is <code>null</code>
   * @throws IllegalArgumentException if <code>clazz</code> doesn't represent
   *         a valid POJO class
   */
  TypeClassInfoImpl(
    StatementManagerImpl mgr,
    RootClassInfoImpl<? super T> rinfo,
    Class<T> clazz,
    boolean dynamic
  ) {
    super(mgr, clazz, RootEntity.class); // search for ctor starting at root
    org.apache.commons.lang3.Validate.isTrue(
      !Modifier.isAbstract(clazz.getModifiers()),
      "type entity class '%s', cannot be abstract", clazz.getSimpleName()
    );
    this.type = findType();
    this.rinfo = rinfo;
    this.dynamic = dynamic;
    // validate the type entity POJO class
    validate(rinfo.getObjectClass());
  }

  /**
   * Finds the annotated type name for this POJO class.
   *
   * @author paouelle
   *
   * @return the non-<code>null</code> annotated type name
   * @throws IllegalArgumentException if the POJO class is improperly annotated
   */
  private String findType() {
    final TypeEntity te = clazz.getAnnotation(TypeEntity.class);

    org.apache.commons.lang3.Validate.isTrue(
      te != null,
      "class '%s' is not annotated with @TypeEntity", clazz.getSimpleName()
    );
    return te.name();
  }

  /**
   * Validates this type entity class.
   *
   * @author paouelle
   *
   * @param  rclazz the non-<code>null</code> class of POJO for the root element
   * @throws IllegalArgumentException if the POJO class is improperly annotated
   */
  private void validate(Class<? super T> rclazz) {
    // check keyspace keys
    getKeyspaceKeys().forEach(
      (n, f) -> {
        org.apache.commons.lang3.Validate.isTrue(
          rclazz.equals(f.getDeclaringClass()),
          "@PartitionKey annotation with name '%s' is not defined in root element class '%s' for type class: %s; found in class: %s",
          n,
          rclazz.getSimpleName(),
          clazz.getSimpleName(),
          f.getDeclaringClass().getSimpleName()
        );
      }
    );
    // check all tables
   tablesImpl().forEach(
     t -> {
       // check keyspace
       org.apache.commons.lang3.Validate.isTrue(
         ReflectionUtils.findFirstClassAnnotatedWith(
           clazz, Keyspace.class
         ).isAssignableFrom(rclazz),
         "@Keyspace annotation is not defined in root element class '%s' for type class: %s",
         rclazz.getSimpleName(),
         clazz.getSimpleName()
       );
       // check table
       org.apache.commons.lang3.Validate.isTrue(
         ReflectionUtils.findFirstClassAnnotatedWith(
           clazz, Table.class
         ).isAssignableFrom(rclazz),
         "@Table annotation is not defined in root element class '%s' for type class: %s",
         rclazz.getSimpleName(),
         clazz.getSimpleName()
       );
       // check partition keys
       t.getPartitionKeys().forEach(
         f -> {
           org.apache.commons.lang3.Validate.isTrue(
             f.getDeclaringClass().isAssignableFrom(rclazz),
             "@PartitionKey annotation with name '%s' is not defined in root element class '%s' for type class: %s",
             f.getColumnName(),
             rclazz.getSimpleName(),
             clazz.getSimpleName()
           );
         }
       );
       // check clustering keys
       t.getClusteringKeys().forEach(
         f -> {
           org.apache.commons.lang3.Validate.isTrue(
             f.getDeclaringClass().isAssignableFrom(rclazz),
             "@ClusteringKey annotation with name '%s' is not defined in root element class '%s' for type class: %s",
             f.getColumnName(),
             rclazz.getSimpleName(),
             clazz.getSimpleName()
           );
         }
       );
       // check type key
       t.getTypeKey().ifPresent(
         f -> {
           org.apache.commons.lang3.Validate.isTrue(
             f.getDeclaringClass().isAssignableFrom(rclazz),
             "@TypeKey annotation with name '%s' is not defined in root element class '%s' for type class: %s",
             f.getColumnName(),
             rclazz.getSimpleName(),
             clazz.getSimpleName()
           );
         }
       );
       // check indexes
       t.getIndexes().forEach(
         f -> {
           org.apache.commons.lang3.Validate.isTrue(
             f.getDeclaringClass().isAssignableFrom(rclazz),
             "@Index annotation with name '%s' is not defined in root element class '%s' for type class: %s",
             f.getColumnName(),
             rclazz.getSimpleName(),
             clazz.getSimpleName()
           );
         }
       );
     }
   );
  }

  /**
   * {@inheritDoc}
   *
   * @author paouelle
   *
   * @see org.helenus.driver.info.TypeClassInfo#getRoot()
   */
  @Override
  @SuppressWarnings("unchecked")
  public RootClassInfoImpl<? super T> getRoot() {
    return rinfo;
  }

  /**
   * {@inheritDoc}
   *
   * @author paouelle
   *
   * @see org.helenus.driver.info.TypeClassInfo#getType()
   */
  @Override
  public String getType() {
    return type;
  }

  /**
   * {@inheritDoc}
   *
   * @author paouelle
   *
   * @see org.helenus.driver.info.TypeClassInfo#isDynamic()
   */
  @Override
  public boolean isDynamic() {
    return dynamic;
  }

  /**
   * Creates a new context for this class info with the given POJO object.
   *
   * @author paouelle
   *
   * @param  object the POJO object
   * @return a non-<code>null</code> newly created context for this class info
   * @throws NullPointerException if <code>object</code> is <code>null</code>
   * @throws IllegalArgumentException if <code>object</code> is not of the
   *         appropriate class
   */
  public POJOContext newContextFromRoot(Object object) {
    try {
      return newContext(clazz.cast(object));
    } catch (ClassCastException e) {
      throw new IllegalArgumentException(e.getMessage(), e);
    }
  }

  /**
   * Converts the specified result row into a POJO object defined by this
   * class information and keyspace key map.
   *
   * @author paouelle
   *
   * @param  row the result row to convert into a POJO
   * @param  type the POJO type extracted from the specified row
   * @param  kkeys a map of keyspace key values to report back into the created
   *         POJO
   * @return the POJO object corresponding to the given result row or <code>null</code>
   *         if the type doesn't match this type entity name
   * @throws NullPointerException if <code>type</code> or <code>kkeys</code>
   *         is <code>null</code>
   * @throws ObjectConversionException if unable to convert to a POJO
   */
  @SuppressWarnings("unchecked")
  public T getObject(Row row, String type, Map<String, Object> kkeys) {
    if (row != null) {
      if (this.type.equals(type)) { // it is our kind
        return super.getObject(row, kkeys);
      }
      final TypeClassInfoImpl<?> tinfo = rinfo.getType(type);

      if (clazz.isAssignableFrom(tinfo.getObjectClass())) {
        // delegate to this sub type info class
        return (T)tinfo.getObject(row, type, kkeys);
      }
    }
    return null;
  }

  /**
   * {@inheritDoc}
   *
   * @author paouelle
   *
   * @see org.helenus.driver.impl.ClassInfoImpl#getObject(com.datastax.driver.core.Row, java.util.Map)
   */
  @Override
  public T getObject(Row row, Map<String, Object> kkeys) {
    if (row == null) {
      return null;
    }
    final ColumnDefinitions cdefs = row.getColumnDefinitions();

    // extract the type so we know which object we are creating
    for (final TableInfoImpl<T> table: getTablesImpl()) {
      final FieldInfoImpl<T> field = table.getTypeKey().orElse(null);

      if (field != null) {
        final int i = cdefs.getIndexOf(field.getColumnName());

        if ((i != -1) && table.getName().equals(cdefs.getTable(i))) {
          final String type = Objects.toString(field.decodeValue(row), null);

          if (type != null) {
            return getObject(row, type, kkeys);
          }
          break;
        }
      }
    }
    throw new ObjectConversionException(clazz, row, "missing POJO type column");
  }

  /**
   * {@inheritDoc}
   *
   * @author paouelle
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return (
      getClass().getSimpleName()
      + "[type=" + type
      + ",clazz=" + clazz
      + ",keyspace=" + getKeyspace()
      + ",columns=" + getColumns()
      + "]"
    );
  }
}

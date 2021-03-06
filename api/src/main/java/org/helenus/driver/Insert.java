/*
 * Copyright (C) 2015-2015 The Helenus Driver Project Authors.
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
package org.helenus.driver;

import java.util.Optional;
import java.util.stream.Stream;

import org.helenus.driver.info.ClassInfo;
import org.helenus.driver.info.TableInfo;

/**
 * The <code>Insert</code> interface extends the functionality of Cassandra's
 * {@link com.datastax.driver.core.querybuilder.Insert} class to provide support
 * for POJOs.
 *
 * @copyright 2015-2016 The Helenus Driver Project Authors
 *
 * @author  The Helenus Driver Project Authors
 * @version 1 - Jan 15, 2015 - paouelle - Creation
 *
 * @param <T> The type of POJO associated with this statement.
 *
 * @since 1.0
 */
public interface Insert<T>
  extends ObjectStatement<T>, BatchableStatement<Void, VoidFuture> {
  /**
   * Checks if there are no column/value pairs registered with this INSERT
   * statement; indicating that all column/value pairs would be inserted.
   * <p>
   * <i>Note:</i> Call {@link #valuesFromObject} would force this method to
   * return <code>true</code>.
   *
   * @author paouelle
   *
   * @return <code>true</code> if there are no column/value pairs registered;
   *         <code>false</code> otherwise
   */
  public boolean isEmpty();

  /**
   * Gets all the tables included in this statement.
   *
   * @author paouelle
   *
   * @return a non-<code>null</code> stream of all tables included in this
   *         statement
   */
  public Stream<TableInfo<T>> tables();

  /**
   * Adds all column/value pairs from the POJO object to the values inserted by
   * this INSERT statement.
   *
   * @author paouelle
   *
   * @return this INSERT statement
   * @throws IllegalStateException if specific values have already been added
   *         to this statement
   */
  public Insert<T> valuesFromObject();

  /**
   * Adds a column/value pair to the values inserted by this INSERT statement.
   * <p>
   * <i>Note:</i> The primary key and mandatory columns are always added to the
   * insert statement.
   *
   * @author paouelle
   *
   * @param  name the name of the column to insert
   * @return this INSERT statement
   * @throws NullPointerException if <code>name</code> is <code>null</code>
   * @throws IllegalArgumentException if the column name is not defined
   *         by the POJO
   */
  public Insert<T> value(String name);

  /**
   * Adds column/value pairs to the values inserted by this INSERT statement.
   * <p>
   * <i>Note:</i> The primary key and mandatory columns are always added to the
   * insert statement.
   *
   * @author paouelle
   *
   * @param  names the names of the columns to insert
   * @return this INSERT statement
   * @throws NullPointerException if any of the column names are <code>null</code>
   * @throws IllegalArgumentException if any of the column names are not defined
   *         by the POJO
   */
  public Insert<T> values(String... names);

  /**
   * Adds a column/value pair to the values inserted by this INSERT statement.
   *
   * @author paouelle
   *
   * @param  name the name of the column to insert
   * @param  value the value to insert for the specified column
   * @return this INSERT statement
   * @throws NullPointerException if <code>name</code> is <code>null</code>
   * @throws IllegalStateException if all columns from the object have already
   *         been added to this statement
   * @throws IllegalArgumentException if any of the column names are not defined
   *         by the POJO
   */
  public Insert<T> value(String name, Object value);

  /**
   * Adds a new options for this INSERT statement.
   *
   * @author paouelle
   *
   * @param  using the option to add.
   * @return the options of this INSERT statement.
   */
  public Options<T> using(Using<?> using);

  /**
   * Gets all options registered with this statement.
   *
   * @author paouelle
   *
   * @return a non-<code>null</code> stream of all options registered with this
   *         statement
   */
  public Stream<Using<?>> usings();

  /**
   * Gets an option registered with this statement given its name
   *
   * @author paouelle
   *
   * @param <U> the type of the option
   *
   * @param  name the name of the option to retrieve
   * @return the registered option with the given name or empty if none registered
   */
  public <U> Optional<Using<U>> getUsing(String name);

  /**
   * Sets the 'IF NOT EXISTS' option for this INSERT statement.
   * <p>
   * An insert with that option will not succeed unless the row does not exist
   * at the time the insertion is execution. The existence check and insertions
   * are done transactionally in the sense that if multiple clients attempt to
   * create a given row with this option, then at most one may succeed.
   * <p>
   * Please keep in mind that using this option has a non negligible performance
   * impact and should be avoided when possible.
   *
   * @author paouelle
   *
   * @return this INSERT statement.
   */
  public Insert<T> ifNotExists();

  /**
   * The <code>Builder</code> interface defines an in-construction INSERT statement.
   *
   * @copyright 2015-2015 The Helenus Driver Project Authors
   *
   * @author  The Helenus Driver Project Authors
   * @version 1 - Mar 20, 2015 - paouelle - Creation
   *
   * @param <T> The type of POJO associated with the statement.
   *
   * @since 1.0
   */
  public interface Builder<T> {
    /**
     * Gets the POJO class associated with this statement builder.
     *
     * @author paouelle
     *
     * @return the POJO class associated with this statement builder
     */
    public Class<T> getObjectClass();

    /**
     * Gets the POJO class information associated with this statement builder.
     *
     * @author paouelle
     *
     * @return the POJO class info associated with this statement builder
     */
    public ClassInfo<T> getClassInfo();

    /**
     * Adds tables to insert into using the keyspace defined in the POJO.
     *
     * @author paouelle
     *
     * @param  tables the names of the tables to insert into
     * @return a newly build INSERT statement that inserts into the specified
     *         tables
     * @throws NullPointerException if <code>tables</code> is <code>null</code>
     * @throws IllegalArgumentException if any of the tables or any of the
     *         referenced columns are not defined by the POJO
     */
    public Insert<T> into(String... tables);

    /**
     * Adds tables to insert into using the keyspace defined in the POJO.
     *
     * @author paouelle
     *
     * @param  tables the names of the tables to insert into
     * @return a newly build INSERT statement that inserts into the specified
     *         tables
     * @throws IllegalArgumentException if any of the tables or any of the
     *         referenced columns are not defined by the POJO
     */
    public Insert<T> into(Stream<String> tables);

    /**
     * Specifies to insert into all tables defined in the POJO using the
     * keyspace defined in the POJO.
     *
     * @author paouelle
     *
     * @return a newly build INSERT statement that inserts into the specified
     *         tables
     * @throws IllegalArgumentException if any of the referenced columns are not
     *         defined by the POJO
     */
    public Insert<T> intoAll();
  }

  /**
   * The <code>Options</code> interface defines the options of an INSERT
   * statement.
   *
   * @copyright 2015-2015 The Helenus Driver Project Authors
   *
   * @author  The Helenus Driver Project Authors
   * @version 1 - Jan 15, 2015 - paouelle - Creation
   *
   * @param <T> The type of POJO associated with the statement.
   *
   * @since 1.0
   */
  public interface Options<T>
    extends Statement<T>, BatchableStatement<Void, VoidFuture> {
    /**
     * Adds the provided option.
     * <p>
     * <i>Note:</i> The primary key and mandatory columns are always added to the
     * INSERT statement.
     *
     * @author paouelle
     *
     * @param  using an INSERT option
     * @return this {@code Options} object
     */
    public Options<T> and(Using<?> using);

    /**
     * Adds a column/value pair to the values inserted by this INSERT statement.
     * <p>
     * <i>Note:</i> The primary key and mandatory columns are always added to the
     * insert statement.
     *
     * @author paouelle
     *
     * @param  name the name of the column to insert
     * @return the INSERT statement those options are part of
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     * @throws IllegalArgumentException if the column name is not defined
     *         by the POJO
     */
    public Insert<T> value(String name);

    /**
     * Adds column/value pairs to the values inserted by this INSERT statement.
     * <p>
     * <i>Note:</i> The primary key and mandatory columns are always added to the
     * insert statement.
     *
     * @author paouelle
     *
     * @param  names the names of the columns to insert
     * @return the INSERT statement those options are part of
     * @throws NullPointerException if any of the column names are <code>null</code>
     * @throws IllegalArgumentException if any of the column names are not defined
     *         by the POJO
     */
    public Insert<T> values(String... names);

    /**
     * Adds a column/value pair to the values inserted by this INSERT statement.
     *
     * @author paouelle
     *
     * @param  name the name of the column to insert
     * @param  value the value to insert for the specified column
     * @return the INSERT statement those options are part of
     * @throws NullPointerException if <code>name</code> is <code>null</code>
     * @throws IllegalArgumentException if any of the column names are not defined
     *         by the POJO
     */
    public Insert<T> value(String name, Object value);
  }
}

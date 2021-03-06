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

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.google.common.util.concurrent.ListenableFuture;

import org.helenus.util.function.ERunnable;

/**
 * The <code>Sequence</code> interface defines support for a statement that
 * can execute a set of statements in sequence returning the result set from
 * the last one only.
 *
 * @copyright 2015-2015 The Helenus Driver Project Authors
 *
 * @author  The Helenus Driver Project Authors
 * @version 1 - Jan 15, 2015 - paouelle - Creation
 *
 * @since 1.0
 */
public interface Sequence
  extends ParentStatement, SequenceableStatement<Void, VoidFuture> {
  /**
   * {@inheritDoc}
   * <p>
   * Gets the keyspace of the first statement in this sequence.
   *
   * @author paouelle
   *
   * @return the keyspace from the first statement in this sequence or
   *         <code>null</code> if the sequence is empty
   *
   * @see org.helenus.driver.GenericStatement#getKeyspace()
   */
  @Override
  public String getKeyspace();

  /**
   * {@inheritDoc}
   * <p>
   * <i>Note:</i> This might not actually be a valid query string if there are
   * more than one statement being sequenced. It will then be a representation
   * of the query strings for each statement similar to a "BATCH" statement.
   *
   * @author paouelle
   *
   * @see org.helenus.driver.GenericStatement#getQueryString()
   */
  @Override
  public String getQueryString();

  /**
   * {@inheritDoc}
   * <p>
   * <i>Note:</i> This method will execute all statements in sequence one after
   * the other until one generates an error or all of them have succeeded.
   *
   * @author paouelle
   *
   * @see org.helenus.driver.GenericStatement#execute()
   */
  @Override
  public Void execute();

  /**
   * {@inheritDoc}
   * <p>
   * <i>Note:</i> This method will execute all statements asynchronously and in
   * sequence one after the other until one generates an error and return a
   * future void result.
   *
   * @author paouelle
   *
   * @see org.helenus.driver.GenericStatement#executeAsync()
   */
  @Override
  public VoidFuture executeAsync();

  /**
   * {@inheritDoc}
   * <p>
   * <i>Note:</i> This method will execute all statements in sequence one after
   * the other until one generates an error and return the result set from that
   * last one.
   *
   * @author paouelle
   *
   * @see org.helenus.driver.GenericStatement#executeRaw()
   */
  @Override
  public ResultSet executeRaw();

  /**
   * {@inheritDoc}
   * <p>
   * <i>Note:</i> This method will execute all statements asynchronously and in
   * sequence one after the other until one generates an error and return a
   * future result set which will allow one to retrieve the result set from that
   * last executed statement.
   *
   * @author paouelle
   *
   * @see org.helenus.driver.GenericStatement#executeAsyncRaw()
   */
  @Override
  public ResultSetFuture executeAsyncRaw();

  /**
   * {@inheritDoc}
   *
   * @author paouelle
   *
   * @see org.helenus.driver.ParentStatement#add(org.helenus.driver.BatchableStatement)
   */
  @Override
  public <R, F extends ListenableFuture<R>> Sequence add(BatchableStatement<R, F> statement);

  /**
   * {@inheritDoc}
   *
   * @author paouelle
   *
   * @see org.helenus.driver.ParentStatement#add(com.datastax.driver.core.RegularStatement)
   */
  @Override
  public Sequence add(com.datastax.driver.core.RegularStatement statement);

  /**
   * Adds a new statement to this sequence.
   *
   * @author paouelle
   *
   * @param <R> The type of result returned when executing the statement to record
   * @param <F> The type of future result returned when executing the statement
   *            to record
   *
   * @param  statement the new statement to add
   * @return this sequence
   * @throws NullPointerException if <code>statement</code> is <code>null</code>
   * @throws ObjectValidationException if the statement's POJO is validated via
   *         an associated recorder and that validation fails
   */
  public <R, F extends ListenableFuture<R>> Sequence add(
    SequenceableStatement<R, F> statement
  );

  /**
   * {@inheritDoc}
   *
   * @author paouelle
   *
   * @see org.helenus.driver.ParentStatement#addErrorHandler(org.helenus.util.function.ERunnable)
   */
  @Override
  public Sequence addErrorHandler(ERunnable<?> run);
}

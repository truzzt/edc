/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.edc.iam.identitytrust.sts.store;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.edc.iam.identitytrust.sts.spi.model.StsClient;
import org.eclipse.edc.iam.identitytrust.sts.spi.store.StsClientStore;
import org.eclipse.edc.iam.identitytrust.sts.store.schema.StsClientStatements;
import org.eclipse.edc.spi.persistence.EdcPersistenceException;
import org.eclipse.edc.spi.query.QuerySpec;
import org.eclipse.edc.spi.result.StoreResult;
import org.eclipse.edc.sql.QueryExecutor;
import org.eclipse.edc.sql.store.AbstractSqlStore;
import org.eclipse.edc.transaction.datasource.spi.DataSourceRegistry;
import org.eclipse.edc.transaction.spi.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.stream.Stream;

import static java.lang.String.format;

public class SqlStsClientStore extends AbstractSqlStore implements StsClientStore {

    private final StsClientStatements statements;

    public SqlStsClientStore(DataSourceRegistry dataSourceRegistry, String dataSourceName, TransactionContext transactionContext,
                             ObjectMapper objectMapper, StsClientStatements statements, QueryExecutor queryExecutor) {
        super(dataSourceRegistry, dataSourceName, transactionContext, objectMapper, queryExecutor);
        this.statements = statements;
    }

    @Override
    public StoreResult<StsClient> create(StsClient client) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                if (findById(connection, client.getId()) != null) {
                    var msg = format(CLIENT_EXISTS_TEMPLATE, client.getId());
                    return StoreResult.alreadyExists(msg);
                }

                queryExecutor.execute(connection, statements.getInsertTemplate(),
                        client.getId(),
                        client.getName(),
                        client.getClientId(),
                        client.getDid(),
                        client.getSecretAlias(),
                        client.getPrivateKeyAlias(),
                        client.getPublicKeyReference(),
                        client.getCreatedAt()
                );

                return StoreResult.success(client);
            } catch (Exception e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public StoreResult<Void> update(StsClient stsClient) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                if (findById(connection, stsClient.getId()) != null) {
                    updateInternal(connection, stsClient);
                    return StoreResult.success();
                } else {
                    return StoreResult.notFound(format(CLIENT_NOT_FOUND_BY_ID_TEMPLATE, stsClient.getId()));
                }
            } catch (Exception e) {
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });
    }

    @Override
    public @NotNull Stream<StsClient> findAll(QuerySpec spec) {
        return transactionContext.execute(() -> {
            Objects.requireNonNull(spec);

            try {
                var queryStmt = statements.createQuery(spec);
                return queryExecutor.query(getConnection(), true, this::mapResultSet, queryStmt.getQueryAsString(), queryStmt.getParameters());
            } catch (SQLException exception) {
                throw new EdcPersistenceException(exception);
            }
        });
    }

    @Override
    public StoreResult<StsClient> findById(String id) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                var client = findById(connection, id);
                if (client == null) {
                    return StoreResult.notFound(format(CLIENT_NOT_FOUND_BY_ID_TEMPLATE, id));
                }
                return StoreResult.success(client);
            } catch (Exception e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public StoreResult<StsClient> findByClientId(String clientId) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                var client = findByClientIdId(connection, clientId);
                if (client == null) {
                    return StoreResult.notFound(format(CLIENT_NOT_FOUND_BY_CLIENT_ID_TEMPLATE, clientId));
                }
                return StoreResult.success(client);
            } catch (Exception e) {
                throw new EdcPersistenceException(e);
            }
        });
    }

    @Override
    public StoreResult<StsClient> deleteById(String id) {
        return transactionContext.execute(() -> {
            try (var connection = getConnection()) {
                var entity = findById(connection, id);
                if (entity != null) {
                    queryExecutor.execute(connection, statements.getDeleteByIdTemplate(), id);
                    return StoreResult.success(entity);
                } else {
                    return StoreResult.notFound(format(CLIENT_NOT_FOUND_BY_ID_TEMPLATE, id));
                }

            } catch (Exception e) {
                throw new EdcPersistenceException(e.getMessage(), e);
            }
        });
    }

    private void updateInternal(Connection connection, StsClient stsClient) {
        queryExecutor.execute(connection, statements.getUpdateTemplate(),
                stsClient.getId(),
                stsClient.getName(),
                stsClient.getClientId(),
                stsClient.getDid(),
                stsClient.getSecretAlias(),
                stsClient.getPrivateKeyAlias(),
                stsClient.getPublicKeyReference(),
                stsClient.getCreatedAt(),
                stsClient.getId());
    }

    private StsClient findById(Connection connection, String id) {
        var sql = statements.getFindByTemplate();
        return queryExecutor.single(connection, false, this::mapResultSet, sql, id);
    }

    private StsClient findByClientIdId(Connection connection, String id) {
        var sql = statements.getFindByClientIdTemplate();
        return queryExecutor.single(connection, false, this::mapResultSet, sql, id);
    }

    private StsClient mapResultSet(ResultSet resultSet) throws Exception {
        return StsClient.Builder.newInstance()
                .id(resultSet.getString(statements.getIdColumn()))
                .did(resultSet.getString(statements.getDidColumn()))
                .name(resultSet.getString(statements.getNameColumn()))
                .clientId(resultSet.getString(statements.getClientIdColumn()))
                .secretAlias(resultSet.getString(statements.getSecretAliasColumn()))
                .privateKeyAlias(resultSet.getString(statements.getPrivateKeyAliasColumn()))
                .publicKeyReference(resultSet.getString(statements.getPublicKeyReferenceColumn()))
                .createdAt(resultSet.getLong(statements.getCreatedAtColumn()))
                .build();
    }
}

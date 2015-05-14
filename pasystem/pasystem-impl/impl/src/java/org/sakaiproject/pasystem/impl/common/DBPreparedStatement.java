package org.sakaiproject.pasystem.impl.common;

import java.io.Reader;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBPreparedStatement {

    private PreparedStatement preparedStatement;
    private int paramCount;

    public DBPreparedStatement(PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
        this.paramCount = 1;
    }

    public DBPreparedStatement param(String parameter) throws SQLException {
        try {
            preparedStatement.setString(paramCount(), parameter);
            return this;
        } catch (SQLException e) {
            cleanup();
            throw e;
        }
    }

    public DBPreparedStatement param(Long parameter) throws SQLException {
        try {
            preparedStatement.setLong(paramCount(), parameter);
            return this;
        } catch (SQLException e) {
            cleanup();
            throw e;
        }
    }

    public DBPreparedStatement param(Integer parameter) throws SQLException {
        try {
            preparedStatement.setInt(paramCount(), parameter);
            return this;
        } catch (SQLException e) {
            cleanup();
            throw e;
        }
    }

    public DBPreparedStatement param(Reader reader) throws SQLException {
        try {
            preparedStatement.setClob(paramCount(), reader);
            return this;
        } catch (SQLException e) {
            cleanup();
            throw e;
        }
    }

    public int executeUpdate() throws SQLException {
        try {
            return preparedStatement.executeUpdate();
        } finally {
            cleanup();
        }
    }

    public DBResults executeQuery() throws SQLException {
        return new DBResults(preparedStatement.executeQuery(),
                preparedStatement);
    }

    private void cleanup() throws SQLException {
        preparedStatement.close();
    }

    private int paramCount() {
        return this.paramCount++;
    }

}

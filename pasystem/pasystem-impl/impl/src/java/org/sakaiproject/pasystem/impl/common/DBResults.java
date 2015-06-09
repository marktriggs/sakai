/**********************************************************************************
 *
 * Copyright (c) 2015 The Sakai Foundation
 *
 * Original developers:
 *
 *   New York University
 *   Payten Giles
 *   Mark Triggs
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **********************************************************************************/

package org.sakaiproject.pasystem.impl.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DBResults implements Iterable<ResultSet>, Iterator<ResultSet>, AutoCloseable {
    private static final Logger LOG = LoggerFactory.getLogger(DBResults.class);
    private PreparedStatement originalStatement;
    private ResultSet resultSet;
    private boolean hasRowReady;

    public DBResults(ResultSet rs, PreparedStatement originalStatement) {
        this.resultSet = rs;
        this.originalStatement = originalStatement;
    }

    public void close() throws SQLException {
        resultSet.close();
        originalStatement.close();
    }

    public boolean hasNext() {
        try {
            if (!hasRowReady) {
                hasRowReady = resultSet.next();
            }

            return hasRowReady;
        } catch (SQLException e) {
            LOG.warn("SQLException while calling hasNext", e);
            return false;
        }
    }

    public ResultSet next() {
        if (!hasRowReady) {
            throw new NoSuchElementException("Read past end of results");
        }

        hasRowReady = false;
        return resultSet;
    }

    public Iterator<ResultSet> iterator() {
        return this;
    }
}

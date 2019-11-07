/*
 * Copyright (c) 2018 Dafiti Group
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 */
package br.com.dafiti.hanger.service;

import br.com.dafiti.hanger.model.Connection;
import br.com.dafiti.hanger.model.EventLog;
import br.com.dafiti.hanger.option.Database;
import br.com.dafiti.hanger.option.EntityType;
import br.com.dafiti.hanger.option.Event;
import br.com.dafiti.hanger.option.Status;
import br.com.dafiti.hanger.repository.ConnectionRepository;
import br.com.dafiti.hanger.security.PasswordCryptor;
import java.security.Principal;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.Transient;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

/**
 *
 * @author Valdiney V GOMES
 */
@Service
public class ConnectionService {

    private final ConnectionRepository connectionRepository;
    private final PasswordCryptor passwordCryptor;
    private final JdbcTemplate jdbcTemplate;
    private final EventLogService eventLogService;

    @Autowired
    public ConnectionService(
            ConnectionRepository connectionRepository,
            PasswordCryptor passwordCryptor,
            JdbcTemplate jdbcTemplate,
            EventLogService eventLogService) {

        this.connectionRepository = connectionRepository;
        this.passwordCryptor = passwordCryptor;
        this.jdbcTemplate = jdbcTemplate;
        this.eventLogService = eventLogService;
    }

    @Cacheable(value = "connections")
    public Iterable<Connection> list() {
        return connectionRepository.findAll();
    }

    /**
     * List all connections and their status.
     *
     * @return list of all connections and their status,
     */
    public List<ConnectionStatus> listConnectionStatus() {
        List<ConnectionStatus> connectionStatus = new ArrayList<>();

        for (Connection connection : this.list()) {
            ConnectionStatus status = new ConnectionStatus();
            status.setConnection(connection);
            status.setStatus(this.testConnection(connection) ? Status.SUCCESS : Status.FAILURE);
            connectionStatus.add(status);
        }

        return connectionStatus;
    }

    public Connection load(Long id) {
        return connectionRepository.findOne(id);
    }

    @Caching(evict = {
        @CacheEvict(value = "connections", allEntries = true)})
    public void save(Connection connection) {
        String password = connection.getPassword();

        password = passwordCryptor.decrypt(password);
        connection.setPassword(passwordCryptor.encrypt(password));
        connectionRepository.save(connection);
    }

    @Caching(evict = {
        @CacheEvict(value = "connections", allEntries = true)})
    public void delete(Long id) {
        connectionRepository.delete(id);
    }

    /**
     * Get a DataSource.
     *
     * @param connection Connection
     * @return Return a DataSource.
     */
    public DataSource getDataSource(Connection connection) {
        SimpleDriverDataSource dataSource = null;

        if (connection != null) {
            try {
                Driver driver = null;
                Properties properties = new Properties();

                switch (connection.getTarget()) {
                    case MYSQL:
                        driver = new com.mysql.jdbc.Driver();
                        properties.setProperty("loginTimeout", "5000");
                        properties.setProperty("connectTimeout", "5000");
                        break;
                    case MSSQL:
                        driver = new com.microsoft.sqlserver.jdbc.SQLServerDriver();
                        properties.setProperty("loginTimeout", "5");
                        break;
                    case POSTGRES:
                        driver = new org.postgresql.Driver();
                        properties.setProperty("loginTimeout", "5");
                        properties.setProperty("connectTimeout", "5");
                        break;
                    case ATHENA:
                        driver = new com.simba.athena.jdbc42.Driver();
                        properties.setProperty("MaxErrorRetry", "10");
                        properties.setProperty("ConnectionTimeout", "5");
                        properties.setProperty("MetadataRetrievalMethod", "Query");
                        break;
                    case HANA:
                        driver = new com.sap.db.jdbc.Driver();
                        properties.setProperty("loginTimeout", "5000");
                        properties.setProperty("connectTimeout", "5000");
                        break;
                    default:
                        break;
                }

                dataSource = new SimpleDriverDataSource(
                        driver,
                        connection.getUrl(),
                        connection.getUsername(),
                        passwordCryptor.decrypt(connection.getPassword()));

                dataSource.setConnectionProperties(properties);
            } catch (SQLException ex) {
                Logger.getLogger(
                        ConnectionService.class.getName())
                        .log(Level.SEVERE, "Fail getting datasource " + connection.getName(), ex);
            }
        }

        return dataSource;
    }

    /**
     * Get the connection.
     *
     * @param connection Connection
     * @return Identify is a connection is ok.
     */
    public boolean testConnection(Connection connection) {
        boolean running = false;
        DataSource datasource = this.getDataSource(connection);

        if (datasource != null) {
            try {
                if (datasource.getConnection() != null) {
                    running = true;
                }
            } catch (SQLException ex) {
                Logger.getLogger(
                        ConnectionService.class.getName())
                        .log(Level.SEVERE, "Fail testing connection to " + connection.getName(), ex);
            } finally {
                try {
                    if (datasource.getConnection() != null) {
                        datasource.getConnection().close();
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(
                            ConnectionService.class.getName())
                            .log(Level.SEVERE, "Fail closing connection", ex.getMessage());
                }
            }
        }

        return running;
    }

    /**
     * Get tables.
     *
     * @param connection Connection
     * @return Database metadata
     */
    @Cacheable(value = "tables", key = "#connection")
    public List<Entity> getTables(Connection connection) {
        List table = new ArrayList();
        DataSource datasource = this.getDataSource(connection);

        try {
            ResultSet tables = datasource.getConnection()
                    .getMetaData()
                    .getTables(null, null, "%", new String[]{"TABLE", "EXTERNAL TABLE"});

            while (tables.next()) {
                table.add(
                        new Entity(
                                tables.getString("TABLE_CAT"),
                                tables.getString("TABLE_SCHEM"),
                                tables.getString("TABLE_NAME"),
                                connection.getTarget()));
            }
        } catch (SQLException ex) {
            Logger.getLogger(
                    ConnectionService.class.getName())
                    .log(Level.SEVERE, "Fail getting metadata of " + connection.getName(), ex);
        } finally {
            try {
                datasource.getConnection().close();
            } catch (SQLException ex) {
                Logger.getLogger(
                        ConnectionService.class.getName())
                        .log(Level.SEVERE, "Fail closing connection", ex.getMessage());
            }
        }

        return table;
    }

    /**
     * Get column.
     *
     * @param connection Connection
     * @param catalog Caralog
     * @param schema Schema
     * @param table Table
     * @return Table columns
     */
    public List<Column> getColumns(
            Connection connection,
            String catalog,
            String schema,
            String table) {

        List column = new ArrayList();
        DataSource datasource = this.getDataSource(connection);

        try {
            ResultSet columns = datasource.getConnection()
                    .getMetaData()
                    .getColumns(catalog, schema, table, null);

            while (columns.next()) {
                column.add(
                        new Column(
                                columns.getInt("ORDINAL_POSITION"),
                                columns.getString("COLUMN_NAME"),
                                columns.getString("TYPE_NAME"),
                                columns.getInt("COLUMN_SIZE"),
                                columns.getInt("DECIMAL_DIGITS"),
                                columns.getString("REMARKS")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(
                    ConnectionService.class.getName())
                    .log(Level.SEVERE, "Fail getting metadata of " + connection.getName(), ex);
        } finally {
            try {
                datasource.getConnection().close();
            } catch (SQLException ex) {
                Logger.getLogger(
                        ConnectionService.class.getName())
                        .log(Level.SEVERE, "Fail closing connection", ex.getMessage());
            }
        }

        return column;
    }

    /**
     * Get primary key.
     *
     * @param connection Connection
     * @param catalog Catalog
     * @param schema Schema
     * @param table Table
     * @return Table primary key
     */
    public List<Column> getPrimaryKey(
            Connection connection,
            String catalog,
            String schema,
            String table) {

        List columns = new ArrayList();
        DataSource datasource = this.getDataSource(connection);

        try {
            ResultSet tables = datasource.getConnection()
                    .getMetaData()
                    .getPrimaryKeys(catalog, schema, table);

            while (tables.next()) {
                columns.add(
                        new Column(
                                tables.getInt("KEY_SEQ"),
                                tables.getString("COLUMN_NAME")));
            }
        } catch (SQLException ex) {
            Logger.getLogger(
                    ConnectionService.class.getName())
                    .log(Level.SEVERE, "Fail getting metadata of " + connection.getName(), ex);
        } finally {
            try {
                datasource.getConnection().close();
            } catch (SQLException ex) {
                Logger.getLogger(
                        ConnectionService.class.getName())
                        .log(Level.SEVERE, "Fail closing connection", ex.getMessage());
            }
        }

        return columns;
    }

    /**
     * Get query resultset.
     *
     * @param connection Connection.
     * @param query Query.
     * @param principal
     * @return Query resultset.
     */
    public QueryResultSet getQueryResultSet(
            Connection connection,
            String query,
            Principal principal) {

        QueryResultSet queryResultSet = new QueryResultSet();
        DataSource datasource = this.getDataSource(connection);

        try {
            //Set a connection to target.
            jdbcTemplate.setDataSource(datasource);
            jdbcTemplate.setMaxRows(100);

            if (connection.getTarget().equals(Database.POSTGRES)
                    || connection.getTarget().equals(Database.ATHENA)) {

                if (!query.toLowerCase().contains(" limit ")) {
                    if (query.endsWith(";")) {
                        query = query.toLowerCase().replaceAll(";", " limit 100;");
                    } else {
                        query = query.concat(" limit 100;");
                    }
                }
            }

            //Execute a query. 
            StopWatch watch = new StopWatch();
            watch.start();
            SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(query);
            watch.stop();

            //Get query elapsed time.
            queryResultSet.setElapsedTime(watch.getTotalTimeMillis());

            //Get query header.
            String[] columnNames = sqlRowSet.getMetaData().getColumnNames();

            queryResultSet.setHeader(
                    Arrays.asList(
                            sqlRowSet
                                    .getMetaData()
                                    .getColumnNames())
            );

            //Get query data.
            while (sqlRowSet.next()) {
                QueryResultSetRow queryResultSetRow = new QueryResultSetRow();

                for (String columnName : columnNames) {
                    queryResultSetRow
                            .getColumn()
                            .add(
                                    sqlRowSet
                                            .getObject(columnName)
                            );
                }

                queryResultSet.getRow().add(queryResultSetRow);
            }

            //Log. 
            String content = "[" + connection.getName() + "] " + query;

            EventLog eventLog = new EventLog();
            eventLog.setUsername(principal.getName());
            eventLog.setDate(new Date());
            eventLog.setType(EntityType.CONNECTION);
            eventLog.setTypeName(content.length() > 255 ? content.substring(0, 250) + "..." : content);
            eventLog.setEvent(Event.QUERY);

            eventLogService.save(eventLog);
        } catch (DataAccessException ex) {
            queryResultSet.setError(ex.getMessage());
        } finally {
            try {
                //Close the connection. 
                jdbcTemplate.getDataSource().getConnection().close();
            } catch (SQLException ex) {
                Logger.getLogger(
                        ConnectionService.class.getName())
                        .log(Level.SEVERE, "Fail closing connection ", ex);
            }
        }

        return queryResultSet;
    }

    /**
     * Refresh connection cache.
     *
     * @param connection
     */
    @Caching(evict = {
        @CacheEvict(value = "tables", key = "#connection")})
    public void evictConnection(Connection connection) {
    }

    /**
     * Represents a target entity.
     */
    public class Entity {

        private String catalog;
        private String schema;
        private String table;
        private Database target;

        public Entity(
                String catalog,
                String schema,
                String table,
                Database target) {

            this.catalog = catalog;
            this.schema = schema;
            this.table = table;
            this.target = target;
        }

        public String getCatalog() {
            return catalog;
        }

        public void setCatalog(String catalog) {
            this.catalog = catalog;
        }

        public String getSchema() {
            return schema;
        }

        public void setSchema(String schema) {
            this.schema = schema;
        }

        public String getTable() {
            return table;
        }

        public void setTable(String table) {
            this.table = table;
        }

        public Database getTarget() {
            return target;
        }

        public void setTarget(Database target) {
            this.target = target;
        }

        @Transient
        public String getCatalogSchema() {
            List<String> catalogSchema = new ArrayList();

            if (catalog != null) {
                catalogSchema.add(catalog);
            }

            if (schema != null) {
                catalogSchema.add(schema);
            }

            return String.join(".", catalogSchema);
        }
    }

    /**
     * Represents a target entity column.
     */
    public class Column {

        int position;
        String name;
        String type;
        int size;
        int decimal;
        String remark;

        public Column(
                int position,
                String name) {

            this.position = position;
            this.name = name;
        }

        public Column(
                int position,
                String name,
                String type,
                int size,
                int decimal,
                String remark) {

            this.position = position;
            this.name = name;
            this.type = type;
            this.size = size;
            this.decimal = decimal;
            this.remark = remark;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public int getDecimal() {
            return decimal;
        }

        public void setDecimal(int decimal) {
            this.decimal = decimal;
        }

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }
    }

    /**
     * Represents a query resultset.
     */
    public class QueryResultSet {

        String error = new String();
        List<String> header = new ArrayList();
        List<QueryResultSetRow> row = new ArrayList();
        long elapsedTime = 0;

        public List<String> getHeader() {
            return header;
        }

        public void setHeader(List<String> header) {
            this.header = header;
        }

        public List<QueryResultSetRow> getRow() {
            return row;
        }

        public void setRow(List<QueryResultSetRow> row) {
            this.row = row;
        }

        public long getElapsedTime() {
            return elapsedTime;
        }

        public void setElapsedTime(long elapsedTime) {
            this.elapsedTime = elapsedTime;
        }

        public boolean hasError() {
            return !error.isEmpty();
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }
    }

    /**
     * Represents a query resultset row.
     */
    public class QueryResultSetRow {

        List<Object> column = new ArrayList();

        public List<Object> getColumn() {
            return column;
        }

        public void setColumn(List<Object> column) {
            this.column = column;
        }
    }

    /**
     * Represents status of a target connection.
     */
    public class ConnectionStatus {

        Status status;
        Connection connection;

        public Status getStatus() {
            return status;
        }

        public void setStatus(Status status) {
            this.status = status;
        }

        public Connection getConnection() {
            return connection;
        }

        public void setConnection(Connection connection) {
            this.connection = connection;
        }
    }
}

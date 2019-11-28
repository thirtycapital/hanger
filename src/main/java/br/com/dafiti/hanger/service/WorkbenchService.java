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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 *
 * @author Fernando Saga
 */
@Service
public class WorkbenchService {

    private final ConnectionService connectionService;

    @Autowired
    public WorkbenchService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * List tree
     *
     * @param connection Connection
     * @param parent String
     * @return List Tree
     */
    public List<Tree> listTree(Connection connection, String parent) {

        List<Tree> listTree = new ArrayList();

        //Identify if node is parent.
        if (parent.equals("#")) {
            listTree = listSchemasTree(connection);
        } else {
            listTree = listTablesTree(connection, parent);
        }

        return listTree;
    }

    /**
     * List schemas tree
     *
     * @param connection Connection
     * @return List schemas tree
     */
    @Cacheable(value = "schemas", key = "#connection")
    public List<Tree> listSchemasTree(Connection connection) {

        List schema = new ArrayList();

        DataSource datasource = connectionService.getDataSource(connection);

        try {
            ResultSet schemas = datasource.getConnection()
                    .getMetaData()
                    .getSchemas();

            while (schemas.next()) {
                schema.add(
                        new Tree(
                                schemas.getString("TABLE_SCHEM"),
                                schemas.getString("TABLE_SCHEM"),
                                "#",
                                "glyphicon glyphicon-th-list",
                                true,
                                null
                        ));
            }

            if (schema.isEmpty()) {
                ResultSet catalogs = datasource.getConnection()
                        .getMetaData()
                        .getCatalogs();

                while (catalogs.next()) {
                    schema.add(
                            new Tree(
                                    catalogs.getString("TABLE_CAT"),
                                    catalogs.getString("TABLE_CAT"),
                                    "#",
                                    "glyphicon glyphicon-th-list",
                                    true,
                                    null
                            ));
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(
                    ConnectionService.class.getName())
                    .log(Level.SEVERE, "Fail getting schema of " + connection.getName(), ex);
        } finally {
            try {
                datasource.getConnection().close();
            } catch (SQLException ex) {
                Logger.getLogger(
                        ConnectionService.class.getName())
                        .log(Level.SEVERE, "Fail closing connection", ex.getMessage());
            }
        }

        return schema;
    }

    /**
     * List tables tree.
     *
     * @param connection Connection
     * @param parent String
     * @return list tables tree
     */
    @Cacheable(value = "tables", key = "#connection")
    public List<Tree> listTablesTree(
            Connection connection,
            String parent) {
        List table = new ArrayList();
        DataSource datasource = connectionService.getDataSource(connection);

        try {

            String catalog = null, schema = null;

            if (connection.getTarget().toString().equals("MYSQL")) {
                catalog = parent;
            } else {
                schema = parent;
            }

            ResultSet tables = datasource.getConnection()
                    .getMetaData()
                    .getTables(
                            catalog,
                            schema,
                            "%",
                            new String[]{"TABLE", "EXTERNAL TABLE"});

            while (tables.next()) {

                TreeAttribute treeAttribute = new TreeAttribute(
                        "fillQuery('" + parent + "', '" + tables.getString("TABLE_NAME") + "', '" + connection.getTarget() + "')",
                        tables.getString("TABLE_CAT"),
                        tables.getString("TABLE_SCHEM"),
                        tables.getString("TABLE_NAME")
                );

                table.add(
                        new Tree(
                                tables.getString("TABLE_NAME"),
                                tables.getString("TABLE_NAME"),
                                parent,
                                "glyphicon glyphicon-th-large",
                                false,
                                treeAttribute
                        ));
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
     * Represents a target entity tree.
     */
    public class Tree {

        String id;
        String text;
        String parent;
        String icon;
        boolean children;
        // Name required to use jstree plugin.
        TreeAttribute a_attr;

        public Tree(
                String id,
                String text,
                String parent,
                String icon,
                boolean children,
                TreeAttribute a_attr) {
            this.id = id;
            this.text = text;
            this.parent = parent;
            this.icon = icon;
            this.children = children;
            this.a_attr = a_attr;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getParent() {
            return parent;
        }

        public void setParent(String parent) {
            this.parent = parent;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public boolean isChildren() {
            return children;
        }

        public void setChildren(boolean children) {
            this.children = children;
        }

        public TreeAttribute getA_attr() {
            return a_attr;
        }

        public void setA_attr(TreeAttribute a_attr) {
            this.a_attr = a_attr;
        }
    }

    /**
     * Represents a target entity tree attributes.
     */
    public class TreeAttribute {

        String ondblclick;
        String catalog;
        String schema;
        String table;

        public TreeAttribute(String ondblclick, String catalog, String schema, String table) {
            this.ondblclick = ondblclick;
            this.catalog = catalog;
            this.schema = schema;
            this.table = table;
        }

        public String getOndblclick() {
            return ondblclick;
        }

        public void setOndblclick(String ondblclick) {
            this.ondblclick = ondblclick;
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
    }
}

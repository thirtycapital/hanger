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
package br.com.dafiti.hanger.controller;

import br.com.dafiti.hanger.exception.Message;
import br.com.dafiti.hanger.model.Connection;
import br.com.dafiti.hanger.service.ConnectionService;
import br.com.dafiti.hanger.service.ConnectionService.QueryResultSet;
import java.security.Principal;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Valdiney V GOMES
 */
@Controller
@RequestMapping(path = "/connection")
public class ConnectionController {

    private final ConnectionService connectionService;

    @Autowired
    public ConnectionController(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * Save a connection.
     *
     * @param connection Connection
     * @param bindingResult
     * @param model Model
     * @return Redirect to list template.
     */
    @PostMapping(path = "/save")
    public String save(
            @Valid @ModelAttribute Connection connection,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("connection", connection);
            return "connection/edit";
        }

        try {
            connectionService.save(connection);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", new Message().getErrorMessage(ex));
        } finally {
            model.addAttribute("connections", connectionService.list());
        }

        return "connection/list";
    }

    /**
     * Add a connection.
     *
     * @param model Model
     * @return Render edit template.
     */
    @GetMapping(path = "/add")
    public String add(Model model) {
        model.addAttribute("connection", new Connection());
        return "connection/edit";
    }

    /**
     * List connections.
     *
     * @param model Model
     * @return Render list template.
     */
    @GetMapping(path = "/list")
    public String list(Model model) {
        model.addAttribute("connections", connectionService.list());
        return "connection/list";
    }

    /**
     * Edit a connection.
     *
     * @param model Model
     * @param connection Connection
     * @return Redirect to edit.
     */
    @GetMapping(path = "/edit/{id}")
    public String edit(
            Model model,
            @PathVariable(value = "id") Connection connection) {

        model.addAttribute("connection", connection);
        return "connection/edit";
    }

    /**
     * Delete a connection.
     *
     * @param id ID
     * @param model Model
     * @return Redirect to list
     */
    @GetMapping(path = "/delete/{id}")
    public String delete(
            @PathVariable(name = "id") Long id,
            Model model) {

        try {
            connectionService.delete(id);
        } catch (Exception ex) {
            if (ex.getClass() == DataIntegrityViolationException.class) {
                model.addAttribute("errorMessage", "This connection is being used. Remove the dependencies before deleting the connection!");
            } else {
                model.addAttribute("errorMessage", "Fail deleting the connection: " + ex.getMessage());
            }
        } finally {
            model.addAttribute("connections", connectionService.list());
        }

        return "connection/list";
    }

    /**
     * Connection tables.
     *
     * @param connection Connection
     * @param model Model
     * @return Render connectionTables template.
     */
    @GetMapping(path = "/{id}/table")
    public String connectionTables(
            @PathVariable(name = "id") Connection connection,
            Model model) {

        try {
            model.addAttribute("connection", connection);
            model.addAttribute("metadata", connectionService.getTables(connection));
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Fail listing tables " + new Message().getErrorMessage(ex));
        }

        return "connection/table";
    }

    /**
     * Connection tables.
     *
     * @param connection Connection
     * @return arrayList with connection tables
     */
    @GetMapping(path = "/{id}/table/list")
    @ResponseBody
    public List<ConnectionService.Entity> getTables(
            @PathVariable(name = "id") Connection connection) {
        return connectionService.getTables(connection);
    }

    /**
     * Connection table columns.
     *
     * @param connection Connection
     * @param catalog Catalog
     * @param schema Schema
     * @param table Table
     * @param model Model
     * @return Render column template.
     */
    @GetMapping(path = "/{id}/table/column/{catalog}/{schema}/{table}")
    public String connectionTableColumns(
            @PathVariable(name = "id") Connection connection,
            @PathVariable(name = "catalog") String catalog,
            @PathVariable(name = "schema") String schema,
            @PathVariable(name = "table") String table,
            Model model) {

        try {
            model.addAttribute("table", table);
            model.addAttribute("pk", connectionService.getPrimaryKey(connection, catalog, schema, table));
            model.addAttribute("column", connectionService.getColumns(connection, catalog, schema, table));
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Fail listing columns " + new Message().getErrorMessage(ex));
        }

        return "connection/column";
    }

    /**
     * Test connections modal.
     *
     * @param model Model
     * @return Test connections modal
     */
    @GetMapping(path = "/test")
    public String testConnectionsModal(Model model) {
        model.addAttribute("connectionStatus", connectionService.listConnectionStatus());
        return "connection/modalTestConnection::connection";
    }

    /**
     * Validate a connection.
     *
     * @param connection Connection
     * @param model Model
     * @return Idenify if the connection is on.
     */
    @GetMapping(path = "/test/{id}")
    public String validate(
            @PathVariable(name = "id") Connection connection,
            Model model) {

        model.addAttribute("connections", connectionService.list());

        try {
            if (connectionService.testConnection(connection)) {
                model.addAttribute("successMessage", connection.getName() + " is connected!");
            } else {
                model.addAttribute("errorMessage", connection.getName() + " is not connected!");
            }
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Fail connection to database " + new Message().getErrorMessage(ex));
        }

        return "connection/list";
    }

    /**
     * Show SQL workbench
     *
     * @param model Model
     * @return SQL workbench template.
     */
    @GetMapping(path = "/workbench/")
    public String workbench(Model model) {
        model.addAttribute("connections", connectionService.list());
        return "connection/workbench";
    }

    /**
     * Workbench query resultset.
     *
     * @param connection Connection
     * @param query SQL Expression
     * @param principal
     * @param model Model
     * @return Query result set fragment.
     */
    @PostMapping(path = "/query/{id}")
    public String query(
            @PathVariable(name = "id") Connection connection,
            @RequestBody String query,
            Principal principal,
            Model model) {

        QueryResultSet queryResultSet = connectionService.getQueryResultSet(connection, query, principal);

        if (queryResultSet.hasError()) {
            model.addAttribute("errorMessage", queryResultSet.getError());
        } else {
            model.addAttribute("resultset", queryResultSet);
        }

        return "connection/fragmentQueryResultSet::resultSet";
    }

    /**
     * Refresh connection cache.
     *
     * @param connection
     */
    @GetMapping(path = "/evict/{id}")
    @ResponseBody
    public void evictConnection(
            @PathVariable(name = "id") Connection connection) {

        connectionService.evictConnection(connection);
    }

    /**
     * Test connections modal.
     *
     * @param connection
     * @param catalog
     * @param schema
     * @param table
     * @param model Model
     * @return Test connections modal
     */
    @GetMapping(path = "/modal/{id}/table/column/{catalog}/{schema}/{table}")
    public String gettableMetadataModal(
            @PathVariable(name = "id") Connection connection,
            @PathVariable(name = "catalog") String catalog,
            @PathVariable(name = "schema") String schema,
            @PathVariable(name = "table") String table,
            Model model) {

        try {
            model.addAttribute("table", table);
            model.addAttribute("pk", connectionService.getPrimaryKey(connection, catalog, schema, table));
            model.addAttribute("column", connectionService.getColumns(connection, catalog, schema, table));
        } catch (Exception ex) {
            model.addAttribute("errorMessage", "Fail listing columns " + new Message().getErrorMessage(ex));
        }

        return "connection/modalTableMetadata::metadata";
    }
}

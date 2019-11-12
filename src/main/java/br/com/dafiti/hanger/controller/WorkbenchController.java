/*
 * Copyright (c) 2019 Dafiti Group
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

import br.com.dafiti.hanger.model.Connection;
import br.com.dafiti.hanger.model.ConnectionQueryStore;
import br.com.dafiti.hanger.service.ConnectionService;
import br.com.dafiti.hanger.service.ConnectionService.QueryResultSet;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author Helio Leal
 * @author Valdiney V GOMES
 */
@Controller
@RequestMapping(path = "/workbench")
public class WorkbenchController {

    private final ConnectionService connectionService;

    @Autowired
    public WorkbenchController(ConnectionService connectionService) {
        this.connectionService = connectionService;
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
        return "workbench/workbench";
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

        return "workbench/fragmentQueryResultSet::resultSet";
    }

    /**
     * Show SQL workbench
     *
     * @param connectionQueryStore
     * @param model Model
     * @return SQL workbench template.
     */
    @GetMapping(path = "/workbench/{id}")
    public String workbench(
            @PathVariable(name = "id") ConnectionQueryStore connectionQueryStore,
            Model model) {
        model.addAttribute("connectionQueryStore", connectionQueryStore);
        model.addAttribute("connections", connectionService.list());

        return "workbench/workbench";
    }
}

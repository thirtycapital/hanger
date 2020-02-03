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

import br.com.dafiti.hanger.exception.Message;
import br.com.dafiti.hanger.model.Connection;
import br.com.dafiti.hanger.model.ConnectionQueryStore;
import br.com.dafiti.hanger.model.User;
import br.com.dafiti.hanger.service.ConnectionQueryStoreService;
import br.com.dafiti.hanger.service.UserService;
import java.security.Principal;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Helio Leal
 */
@Controller
@RequestMapping(path = "/query")
public class ConnectionQueryStoreController {

    private final ConnectionQueryStoreService connectionQueryStoreService;
    private final UserService userService;

    @Autowired
    public ConnectionQueryStoreController(
            ConnectionQueryStoreService connectionQueryStoreService,
            UserService userService) {

        this.connectionQueryStoreService = connectionQueryStoreService;
        this.userService = userService;
    }

    /**
     * Query store modal.
     *
     * @param connection
     * @param query
     * @param model Model
     * @param principal
     *
     * @return Test connections modal
     */
    @PostMapping(path = "/store/modal/{id}")
    public String queryStoreModal(
            @PathVariable(name = "id") Connection connection,
            @RequestBody(required = false) String query,
            Model model,
            Principal principal) {

        User user = userService.findByUsername(principal.getName());

        if (user != null) {
            ConnectionQueryStore connectionQueryStore
                    = new ConnectionQueryStore(connection, user);

            if (query != null && !query.isEmpty()) {
                connectionQueryStore.setQuery(query);
            }
            model.addAttribute("connectionQueryStore", connectionQueryStore);
        }

        return "workbench/modalQueryStore::query";
    }

    /**
     * Save a connection query store.
     *
     * @param redirectAttributes
     * @param connectionQueryStore
     * @param model
     * @param principal
     *
     * @return redirect to a page.
     */
    @PostMapping(path = "/save")
    public String save(
            RedirectAttributes redirectAttributes,
            @Valid @ModelAttribute ConnectionQueryStore connectionQueryStore,
            Model model,
            Principal principal) {
        boolean update = connectionQueryStore.getId() != null;
        String redirect = "redirect:/query/list/";

        try {
            connectionQueryStoreService.save(connectionQueryStore);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Query successfully stored!");

            if (!update) {
                redirect = "redirect:/workbench/workbench/"
                        .concat(connectionQueryStore.getId().toString());
            }
        } catch (Exception ex) {
            model.addAttribute("connectionQueryStore", connectionQueryStore);
            redirectAttributes.addFlashAttribute("errorMessage",
                    new Message().getErrorMessage(ex));
        }

        return redirect;
    }

    /**
     * List queries.
     *
     * @param model
     * @param principal
     * @return
     */
    @GetMapping(path = "/list")
    public String list(
            Model model,
            Principal principal) {

        User user = userService.findByUsername(principal.getName());

        if (user != null) {
            model.addAttribute("connectionQueryStoreList",
                    this.connectionQueryStoreService.findByUserOrSharedTrue(user));
        }

        return "workbench/list";
    }

    /**
     * Call query store modal.
     *
     * @param connectionQueryStore
     * @param model Model
     * @param principal
     *
     * @return Test connections modal
     */
    @GetMapping(path = "/load/modal/{id}")
    public String queryLoadModal(
            @PathVariable(name = "id") ConnectionQueryStore connectionQueryStore,
            Model model,
            Principal principal) {

        User user = userService.findByUsername(principal.getName());

        if (user != null) {
            model.addAttribute("connectionQueryStore", connectionQueryStore);
        }

        return "workbench/modalQueryStore::query";
    }

    /**
     * Delete a connection query store.
     *
     * @param id
     * @param model
     * @param principal
     * @return
     */
    @GetMapping(path = "/delete/{id}")
    public String delete(
            @PathVariable(name = "id") Long id,
            Model model,
            Principal principal) {

        try {
            connectionQueryStoreService.delete(id);
        } catch (Exception ex) {
            model.addAttribute("errorMessage",
                    "Fail deleting the connection query: " + ex.getMessage());
        }

        return this.list(model, principal);
    }
}

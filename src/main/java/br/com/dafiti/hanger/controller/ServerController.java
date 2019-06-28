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
import br.com.dafiti.hanger.model.Server;
import br.com.dafiti.hanger.service.JenkinsService;
import br.com.dafiti.hanger.service.ServerService;
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

/**
 *
 * @author Valdiney V GOMES
 */
@Controller
@RequestMapping(path = "/server")
public class ServerController {

    private final ServerService serverService;
    private final JenkinsService jenkinsService;

    @Autowired
    public ServerController(ServerService serverService, JenkinsService jenkinsService) {
        this.serverService = serverService;
        this.jenkinsService = jenkinsService;
    }

    /**
     * Save a server.
     *
     * @param server
     * @param bindingResult
     * @param model
     * @return
     */
    @PostMapping(path = "/save")
    public String saveServer(@Valid @ModelAttribute Server server, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("server", server);
            return "server/edit";
        }

        try {
            serverService.save(server);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", new Message().getErrorMessage(ex));
        } finally {
            model.addAttribute("servers", serverService.list());
        }

        return "server/list";
    }

    /**
     * Add a server.
     *
     * @param model
     * @return
     */
    @GetMapping(path = "/add")
    public String addServer(Model model) {
        model.addAttribute("server", new Server());
        return "server/edit";
    }

    /**
     * List servers.
     *
     * @param model
     * @return
     */
    @GetMapping(path = "/list")
    public String listServer(Model model) {
        model.addAttribute("servers", serverService.list());
        return "server/list";
    }

    /**
     * Edit a server.
     *
     * @param model
     * @param id
     * @return
     */
    @GetMapping(path = "/edit/{id}")
    public String editServer(Model model, @PathVariable(value = "id") Long id) {
        model.addAttribute("server", serverService.load(id));
        return "server/edit";
    }

    /**
     * Delete a server.
     *
     * @param id
     * @param model
     * @return
     */
    @GetMapping(path = "/delete/{id}")
    public String deleteServer(@PathVariable(name = "id") Long id, Model model) {
        try {
            serverService.delete(id);
        } catch (Exception ex) {
            if (ex.getClass() == DataIntegrityViolationException.class) {
                model.addAttribute("errorMessage", "This server is being used. Remove the dependencies before deleting the server!");
            } else {
                model.addAttribute("errorMessage", "Fail deleting the server: " + ex.getMessage());
            }
        } finally {
            model.addAttribute("servers", serverService.list());
        }

        return "server/list";
    }

    /**
     * Test a server.
     *
     * @param server
     * @param model
     * @return
     */
    @GetMapping(path = "/test/{id}")
    public String serverIsRunning(@PathVariable(name = "id") Server server, Model model) {
        model.addAttribute("servers", serverService.list());

        if (jenkinsService.isRunning(server)) {
            if (jenkinsService.hasNotificationPlugin(server)) {
                model.addAttribute("successMessage", server.getName() + " is running and notification plugin is deployed!");
            } else {
                model.addAttribute("errorMessage", server.getName() + " is running but notification plugin is not deployed!");
            }
        } else {
            model.addAttribute("errorMessage", server.getName() + " is not running!");
        }

        return "server/list";
    }

}

/*
 * Copyright (c) 2021 Dafiti Group
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
import br.com.dafiti.hanger.model.TriggerDetail;
import br.com.dafiti.hanger.service.ServerService;
import br.com.dafiti.hanger.service.TriggerService;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *
 * @author Helio Leal
 */
@Controller
@RequestMapping(path = "/trigger")
public class TriggerController {

    private final TriggerService triggerService;
    private final ServerService serverService;

    @Autowired
    public TriggerController(
            TriggerService triggerService,
            ServerService serverService) {
        this.triggerService = triggerService;
        this.serverService = serverService;
    }

    /**
     * List triggers.
     *
     * @param model Model
     * @return Triggers list template.
     */
    @GetMapping(path = "/list")
    public String list(Model model) {
        model.addAttribute("triggerDetails", this.triggerService.list());
        return "trigger/list";
    }

    /**
     * Add a trigger.
     *
     * @param model Model
     * @return Server edit template.
     */
    @GetMapping(path = "/add")
    public String add(Model model) {
        model.addAttribute("triggerDetail", new TriggerDetail());
        model.addAttribute("servers", serverService.list());

        return "trigger/edit";
    }

    /**
     * Save a trigger.
     *
     * @param triggerDetail TriggerDetail
     * @param bindingResult BindingResult
     * @param model Model
     * @return Trigger list template.
     */
    @PostMapping(path = "/save")
    public String saveServer(@Valid @ModelAttribute TriggerDetail triggerDetail, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("triggerDetail", triggerDetail);
            return "trigger/edit";
        }

        try {
            this.triggerService.save(triggerDetail);
        } catch (Exception ex) {
            model.addAttribute("errorMessage", new Message().getErrorMessage(ex));
        } finally {
            model.addAttribute("triggerDetails", this.triggerService.list());
        }

        return "redirect:/trigger/list";
    }
}

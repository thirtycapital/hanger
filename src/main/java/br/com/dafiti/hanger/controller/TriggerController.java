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
import br.com.dafiti.hanger.model.Job;
import br.com.dafiti.hanger.model.TriggerDetail;
import br.com.dafiti.hanger.service.JobService;
import br.com.dafiti.hanger.service.TriggerService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 *
 * @author Helio Leal
 */
@Controller
@RequestMapping(path = "/trigger")
public class TriggerController {

    private final TriggerService triggerService;
    private final JobService jobService;

    @Autowired
    public TriggerController(
            TriggerService triggerService,
            JobService jobService) {
        this.triggerService = triggerService;
        this.jobService = jobService;
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
        this.modelDefault(model, new TriggerDetail());
        return "trigger/edit";
    }

    /**
     * Save a trigger.
     *
     * @param triggerDetail TriggerDetail
     * @param bindingResult BindingResult
     * @param redirectAttributes RedirectAttributes
     * @param model Model
     * @param insert Identify action between insert or update.
     * @return Trigger list template.
     */
    @PostMapping(path = "/save")
    public String save(
            @Valid @ModelAttribute TriggerDetail triggerDetail,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model,
            @RequestParam(name = "insert", required = false) boolean insert) {

        if (bindingResult.hasErrors()) {
            this.modelDefault(model, triggerDetail, insert);
            return "trigger/edit";
        }

        try {
            if (insert) {
                this.triggerService.save(triggerDetail);
            } else {
                this.triggerService.update(triggerDetail);
            }
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", new Message().getErrorMessage(ex));
        } finally {
            model.addAttribute("triggerDetails", this.triggerService.list());
        }

        return "redirect:/trigger/list";
    }

    /**
     * Job list modal.
     *
     * @param model Model
     * @return Job list modal
     */
    @GetMapping(path = "/modal/list")
    public String jobListModal(Model model) {
        model.addAttribute("jobs", jobService.listFromCache());
        return "trigger/modalJobList::job";
    }

    /**
     * Add a job.
     *
     * @param triggerDetail
     * @param jobList
     * @param bindingResult
     * @param model model
     * @param insert Identify action between insert or update
     * @return Job edit
     */
    @PostMapping(path = "/save", params = {"partial_add_job"})
    public String addJob(
            @ModelAttribute TriggerDetail triggerDetail,
            @RequestParam(value = "jobList", required = false) List<Job> jobList,
            @RequestParam(name = "insert", required = false) boolean insert,
            BindingResult bindingResult,
            Model model) {
        jobList.forEach((job) -> {
            triggerDetail.addJob(job);
        });

        this.modelDefault(model, triggerDetail, insert);
        return "trigger/edit";
    }

    /**
     * Delete a trigger.
     *
     * @param redirectAttributes RedirectAttributes
     * @param triggerName String trigger name
     * @return Redirect to job list
     */
    @GetMapping(path = "/delete/{triggerName}")
    @ResponseBody
    public boolean delete(
            RedirectAttributes redirectAttributes,
            @PathVariable(name = "triggerName") String triggerName) {

        try {
            this.triggerService.delete(triggerName);
            return true;
        } catch (Exception ex) {
            redirectAttributes.addAttribute("errorMessage", new Message().getErrorMessage(ex));
        }

        return false;
    }

    /**
     * Edit a trigger.
     *
     * @param model Model
     * @param redirectAttributes
     * @param triggerName
     * @return trigger edit
     */
    @GetMapping(path = "/edit/{triggerName}")
    public String edit(
            Model model,
            RedirectAttributes redirectAttributes,
            @PathVariable(value = "triggerName") String triggerName) {
        try {
            this.modelDefault(model, this.triggerService.get(triggerName), false);
        } catch (Exception ex) {
            redirectAttributes.addAttribute("errorMessage", new Message().getErrorMessage(ex));
        }

        return "trigger/edit";
    }

    /**
     * Default model.
     *
     * @param model Model
     * @triggerDetail TriggerDetail
     */
    private void modelDefault(Model model, TriggerDetail triggerDetail) {
        this.modelDefault(model, triggerDetail, true);
    }

    /**
     * Default model
     *
     * @param model Model
     * @triggerDetail TriggerDetail
     * @param insert boolean
     */
    private void modelDefault(Model model, TriggerDetail triggerDetail, boolean insert) {
        model.addAttribute("triggerDetail", triggerDetail);
        model.addAttribute("insert", insert);
    }
}

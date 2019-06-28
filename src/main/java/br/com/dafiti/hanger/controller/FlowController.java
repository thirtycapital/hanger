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

import br.com.dafiti.hanger.model.Job;
import br.com.dafiti.hanger.service.FlowService;
import br.com.dafiti.hanger.service.JobApprovalService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Valdiney V GOMES
 */
@Controller
public class FlowController {

    private final FlowService flowService;
    private final JobApprovalService jobApprovalService;

    @Autowired
    public FlowController(FlowService flowService, JobApprovalService jobApprovalService) {
        this.flowService = flowService;
        this.jobApprovalService = jobApprovalService;
    }

    /**
     * Show the job flow.
     *
     * @param expanded Identify if should show the flow expanded
     * @param principal logged user
     * @param job Job
     * @param model Model
     * @return Flow display
     */
    @GetMapping(path = "/flow/job/{id}")
    public String flow(
            @RequestParam(value = "expanded", required = false) boolean expanded,
            Principal principal,
            @PathVariable("id") Job job,
            Model model) {

        if (job != null) {
            model.addAttribute("job", job);
            model.addAttribute("warnings", flowService.getFlowWarning(job));
            model.addAttribute("chart", flowService.getJobFlow(job, false, expanded));
            model.addAttribute("approval", this.jobApprovalService.hasApproval(job, principal));
        }

        return "flow/display";
    }

    /**
     * Show the job propagation flow.
     *
     * @param principal logged user
     * @param job Job
     * @param model Model
     * @return Flow display
     */
    @GetMapping(path = {"/flow/propagation/job/{id}", "/propagation/job/{id}"})
    public String propagation(
            Principal principal,
            @PathVariable("id") Job job,
            Model model) {

        if (job != null) {
            model.addAttribute("job", job);
            model.addAttribute("chart", flowService.getJobFlow(job, true, false));
            model.addAttribute("approval", this.jobApprovalService.hasApproval(job, principal));
        }

        return "flow/display";
    }

    /**
     * Show the job flow warning modal.
     *
     * @param job Job
     * @param model Model
     * @return flow modal
     */
    @GetMapping(path = "/flow/warning/{id}")
    public String warning(
            @PathVariable("id") Job job,
            Model model) {

        if (job != null) {
            model.addAttribute("warnings", flowService.getFlowWarning(job));
        }

        return "flow/modalWarning::warning";
    }
}

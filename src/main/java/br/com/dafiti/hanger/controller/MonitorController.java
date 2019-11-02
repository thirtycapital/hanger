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
import br.com.dafiti.hanger.model.JobDetails;
import br.com.dafiti.hanger.service.JobDetailsService;
import br.com.dafiti.hanger.model.Subject;
import br.com.dafiti.hanger.model.SubjectDetails;
import br.com.dafiti.hanger.service.JobService;
import br.com.dafiti.hanger.service.SubjectDetailsService;
import br.com.dafiti.hanger.service.SubjectService;
import br.com.dafiti.hanger.service.UserService;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author Guilherme Almeida
 * @author Valdiney V GOMES
 */
@Controller
@RequestMapping(path = {"/", "/home"})
public class MonitorController {

    private final JobService jobService;
    private final SubjectService subjectService;
    private final JobDetailsService jobDetailsService;
    private final SubjectDetailsService subjectDetailsService;
    private final UserService userService;

    @Autowired
    public MonitorController(
            JobService jobService,
            SubjectService subjectService,
            JobDetailsService jobDetails,
            SubjectDetailsService subjectDetailsService,
            UserService userService) {

        this.jobService = jobService;
        this.subjectService = subjectService;
        this.jobDetailsService = jobDetails;
        this.subjectDetailsService = subjectDetailsService;
        this.userService = userService;
    }

    /**
     * Show the subjectDetail page.
     *
     * @param model Model
     * @return Home template
     */
    @GetMapping(path = {"/", "/home"})
    public String subjectDetail(Model model) {
        model.addAttribute("subjectDetails", subjectDetailsService.getDetailsOf(subjectService.findBySubscription()));

        modelDefault(model);

        return "monitor/monitor";
    }

    /**
     * List jobs by subject.
     *
     * @param principal Principal
     * @param model Model
     * @param request HttpServletRequest
     * @param id Subject ID
     * @return Home template
     */
    @GetMapping("/detail/{id}")
    public String jobDetail(
            Principal principal,
            Model model,
            HttpServletRequest request,
            @PathVariable(value = "id") String id) {

        Subject subject = new Subject();
        List<Job> jobs = new ArrayList();

        if (id.equalsIgnoreCase("all")) {
            jobs = (List) jobService.list();
        } else if (StringUtils.isNumeric(id)) {
            subject = subjectService.load(Long.valueOf(id));

            if (subject != null) {
                jobs = jobService.findBySubjectOrderByName(subject);
            }
        }

        modelDefault(model);
        modelDetails(model, subject, principal, jobs);

        return "monitor/monitor";
    }

    /**
     * Add a job into a subject.
     *
     * @param jobsID Job list ID
     * @param subject Subject
     * @param model Model
     * @return Redirect to subject template
     */
    @PostMapping(path = "/detail/add/{id}", params = {"addJobs"})
    public String addSubject(
            @RequestParam(value = "jobList", required = false, defaultValue = "") String jobsID,
            @PathVariable(value = "id") Subject subject,
            Model model) {

        if (!jobsID.isEmpty()) {
            for (String jobId : jobsID.split(",")) {
                Job job = jobService.load(Long.parseLong(jobId));

                if (job != null) {
                    if (!job.getSubject().contains(subject)) {
                        job.addSubject(subject);
                        jobService.save(job);
                    }
                }
            }
        }

        model.addAttribute("currentSubject", subject);
        modelDefault(model);

        return "redirect:/detail/" + subject.getId();
    }

    /**
     * Remove a subject from a job.
     *
     * @param job Job
     * @param subject Subject
     * @param model Model
     * @return Redirect to subject template
     */
    @GetMapping(path = "/detail/remove/{jobID}/{subjectID}")
    public String removeSubject(
            @PathVariable(value = "jobID") Job job,
            @PathVariable(value = "subjectID") Subject subject,
            Model model
    ) {

        job.getSubject().remove(subject);
        jobService.save(job);

        model.addAttribute("currentSubject", subject);
        modelDefault(model);

        return "redirect:/detail/" + subject.getId();
    }

    /**
     * Job list modal.
     *
     * @param subject Subject
     * @param model Model
     * @return Modal template
     */
    @GetMapping(path = "/detail/modal/{subjectID}")
    public String jobModal(
            @PathVariable(value = "subjectID") Subject subject,
            Model model
    ) {

        model.addAttribute("subject", subject);
        model.addAttribute("jobs", jobService.list());

        return "monitor/modalJobList::job";
    }

    /**
     * Model default attribute.
     *
     * @param model Model
     */
    private void modelDefault(Model model) {
        model.addAttribute("all", jobService.count());
        model.addAttribute("subjectSummary", subjectDetailsService.getSummaryOf(subjectService.findBySubscription()));
        model.addAttribute("loggedIn", userService.getLoggedIn());
        model.addAttribute("subjects", subjectService.findBySubscription());
    }

    /**
     * Model details
     *
     * @param model Model
     * @param subject Subject
     * @param jobs Jobs
     */
    private void modelDetails(
            Model model,
            Subject subject,
            Principal principal,
            List<Job> jobs) {

        model.addAttribute("currentSubject", subject);

        if (principal != null || jobs != null) {
            List<JobDetails> jobDetails = jobDetailsService.getDetailsOf(jobs, principal);
            model.addAttribute("jobDetails", jobDetails
                    .stream()
                    .sorted((a, b) -> (a.getJob().getName().compareTo(b.getJob().getName())))
                    .sorted((a, b) -> a.getStatus().toString().compareTo(b.getStatus().toString())).collect(Collectors.toList()));
        }
    }
}

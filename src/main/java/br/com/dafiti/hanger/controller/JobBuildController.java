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
import br.com.dafiti.hanger.model.JobBuildMetricFilter;
import br.com.dafiti.hanger.option.Phase;
import br.com.dafiti.hanger.service.JobBuildGraphService;
import br.com.dafiti.hanger.service.JobService;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Valdiney V GOMES
 */
@Controller
@RequestMapping("/build")
public class JobBuildController {

    private final JobBuildGraphService jobBuildGraphService;
    private final JobService jobService;

    @Autowired
    public JobBuildController(
            JobBuildGraphService jobBuildGraphService,
            JobService jobService) {

        this.jobBuildGraphService = jobBuildGraphService;
        this.jobService = jobService;
    }

    /**
     * Show build heatmaps.
     *
     * @param model Model
     * @return Job build heatmap
     */
    @GetMapping(path = {"/heatmap", "/heatmap/filtered"})
    public String heatmap(Model model) {
        model.addAttribute("filter", new JobBuildMetricFilter(Phase.STARTED, new Date(), new Date()));
        model.addAttribute("detail", jobBuildGraphService.getJobBuildDetail(Phase.STARTED, new Date(), new Date()));
        model.addAttribute("total", jobBuildGraphService.getJobBuildTotal(Phase.STARTED, new Date(), new Date()));
        return "build/heatmap";
    }

    /**
     * Apply build heatmap filter.
     *
     * @param model Model
     * @param from Date from
     * @param to Date to
     * @param phase Phase
     * @return Job build heatmap
     */
    @PostMapping(path = "/heatmap/filtered")
    public String heatmapFilter(
            @RequestParam("phase") Phase phase,
            @RequestParam("dateFrom") String from,
            @RequestParam("dateTo") String to,
            Model model) {

        Date dateFrom = new Date();
        Date dateTo = new Date();

        try {
            dateFrom = new SimpleDateFormat("yyyy-MM-dd").parse(from);
            dateTo = new SimpleDateFormat("yyyy-MM-dd").parse(to);
        } catch (ParseException ex) {
            Logger.getLogger(JobBuildController.class.getName()).log(Level.SEVERE, "Fail parsing date!", ex);
        }

        model.addAttribute("filter", new JobBuildMetricFilter(phase, dateFrom, dateTo));
        model.addAttribute("detail", jobBuildGraphService.getJobBuildDetail(phase, dateFrom, dateTo));
        model.addAttribute("total", jobBuildGraphService.getJobBuildTotal(phase, dateFrom, dateTo));
        return "build/heatmap";
    }

    /**
     * Show build gantt.
     *
     * @param model Model
     * @return Job build gantt
     */
    @GetMapping({"/gantt"})
    public String gantt(Model model) {
        model.addAttribute("filter", new JobBuildMetricFilter(Phase.STARTED, new Date(), new Date()));
        model.addAttribute("jobs", "");
        model.addAttribute("chart", "");
        return "build/gantt";
    }

    /**
     * build a modal bootstrap object with a list of jobs to select in the gantt
     * chart filter
     *
     * @param model
     * @return
     */
    @GetMapping({"/gantt/list"})
    public String loadJobListModal(Model model) {
        model.addAttribute("jobs", jobService.listFromCache());
        return "build/modalJobList::job";
    }

    /**
     * Apply the filter to select data and return an js array to build gantt
     * chart with ajax.
     *
     * @param from Date from
     * @param jobs Job list
     * @param sliderHour
     * @param model Model
     * @return Build heatmap template
     */
    @GetMapping("/gantt/filtered")
    @ResponseBody
    public String ganttFilter(
            @RequestParam("dateFrom") String from,
            @RequestParam("slider_hour") String sliderHour,
            @RequestParam(value = "jobs", required = false) List<Job> jobs,
            Model model) {

        Date dateFrom = new Date();

        try {
            dateFrom = new SimpleDateFormat("yyyy-MM-dd").parse(from);
        } catch (ParseException ex) {
            Logger.getLogger(JobBuildController.class.getName()).log(Level.SEVERE, "Fail parsing date!", ex);
        }
        int hourFrom = Integer.parseInt(sliderHour.split(",")[0]);
        int hourTo = Integer.parseInt(sliderHour.split(",")[1]);

        return jobBuildGraphService.getGanttData(jobs, dateFrom, dateFrom, hourFrom, hourTo);
    }
}

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

import br.com.dafiti.hanger.model.Job;
import br.com.dafiti.hanger.model.JobBuildMetric;
import br.com.dafiti.hanger.option.Phase;
import br.com.dafiti.hanger.repository.JobBuildRepository;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 *
 * @author Valdiney V GOMES
 */
@Service
public class JobBuildGraphService {

    private final JobBuildRepository jobBuildRepository;

    @Autowired
    public JobBuildGraphService(JobBuildRepository jobBuildRepository) {
        this.jobBuildRepository = jobBuildRepository;
    }

    /**
     * Find job build count by hour.
     *
     * @param phase Phase
     * @param startDate Start Date
     * @param endDate End Date
     *
     * @return Job build count by hour list.
     */
    @Cacheable(value = "jobBuildByHour", key = "{#phase, #startDate, #endDate}")
    public List<JobBuildMetric> findJobBuildCountByHour(
            Phase phase,
            Date startDate,
            Date endDate) {

        Date initialDate = new DateTime(startDate)
                .withTimeAtStartOfDay()
                .toDate();

        Date finalDate = new DateTime(endDate)
                .withTimeAtStartOfDay()
                .plusHours(23)
                .plusMinutes(59)
                .plusSeconds(59)
                .toDate();

        return jobBuildRepository.findJobBuildCountByHour(phase, initialDate, finalDate);
    }

    /**
     * Find job time by phase and nunber
     *
     * @param job Job
     * @param startDate Start Date
     * @param endDate End Date
     * @param hourFrom Hour from
     * @param hourTo Hour To
     * @return job time by phase and nunber
     */
    public List<JobBuildMetric> findJobBuildByNumber(
            List<Job> job,
            Date startDate,
            Date endDate,
            int hourFrom,
            int hourTo) {

        Date initialDate = new DateTime(startDate)
                .withTimeAtStartOfDay()
                .plusHours(hourFrom)
                .toDate();

        Date finalDate = new DateTime(endDate)
                .withTimeAtStartOfDay()
                .plusHours(hourTo)
                .plusMinutes(59)
                .plusSeconds(59)
                .toDate();

        List<JobBuildMetric> metrics;

        if (job == null || job.isEmpty()) {
            metrics = jobBuildRepository.findJobBuildByNumber(initialDate, finalDate);
        } else {
            metrics = jobBuildRepository.findJobBuildByNumber(job, initialDate, finalDate);
        }

        return metrics;
    }

    /**
     * Get the job build summary by job and hour in a 24 hours window.
     *
     * @param phase Phase
     * @param dateFrom Date From
     * @param dateTo Date To
     * @return Job build summary by job and hour.
     */
    public Map<Job, Long[]> getJobBuildDetail(
            Phase phase,
            Date dateFrom,
            Date dateTo) {

        Map<Job, Long[]> summary = new HashMap();

        //List all build by hour. 
        List<JobBuildMetric> builds = this.findJobBuildCountByHour(phase, dateFrom, dateTo);

        //List the build information in a 24 hours.
        builds.stream().forEach((data) -> {
            Job key = data.getJob();
            Long[] value = summary.containsKey(key) ? summary.get(key) : new Long[24];
            value[data.getHour()] = data.getBuild();
            summary.put(key, value);

        });

        return summary;
    }

    /**
     * Get the job build total by job and hour in a 24 hours window.
     *
     * @param phase Phase
     * @param dateFrom Date From
     * @param dateTo Date To
     * @return Job build summary summary by job and hour.
     */
    public Long[] getJobBuildTotal(
            Phase phase,
            Date dateFrom,
            Date dateTo) {

        Long[] value = new Long[24];

        //List all build by hour.
        List<JobBuildMetric> builds = this.findJobBuildCountByHour(phase, dateFrom, dateTo);

        //Aggregate the build information in a 24 hours.
        builds.stream().forEach((data) -> {
            Long build = value[data.getHour()];

            if (build == null) {
                value[data.getHour()] = data.getBuild();
            } else {
                value[data.getHour()] += data.getBuild();
            }
        });

        for (int i = 0; i < value.length; i++) {
            if (value[i] == null) {
                value[i] = 0L;
            }
        }

        return value;
    }

    /**
     * Get elapsed time between two dates.
     *
     * @param dateFrom Date from
     * @param dateTo Date To
     * @return Elapsed time
     */
    private String getElapsetTime(Date dateFrom, Date dateTo) {
        Period period = new Period(new DateTime(dateFrom), new DateTime(dateTo));
        return StringUtils.leftPad(String.valueOf(period.getHours()), 2, "0")
                + ":" + StringUtils.leftPad(String.valueOf(period.getMinutes()), 2, "0")
                + ":" + StringUtils.leftPad(String.valueOf(period.getSeconds()), 2, "0");
    }

    /**
     * Get the gantt graph data.
     *
     * @param job Job
     * @param dateFrom Date from
     * @param dateTo Date to
     * @param hourFrom Hour from
     * @param hourTo Hour to
     * @return Gantt
     */
    public String getGanttData(List<Job> job, Date dateFrom, Date dateTo, int hourFrom, int hourTo) {
        StringBuilder js = new StringBuilder();
        js.append("[");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<JobBuildMetric> metrics = this.findJobBuildByNumber(job, dateFrom, dateTo, hourFrom, hourTo);
        int metricsSize = metrics.size();
        int index = 1;

        for (JobBuildMetric jobBuildMetrics : metrics) {
            String tooltip = "<pre style='background:white;border:none;width:300px;'>"
                    + "<br/>Start: " + sdf.format(jobBuildMetrics.getQueueDate())
                    + "<br/>Finish: " + sdf.format(jobBuildMetrics.getFinishDate())
                    + "<br/>Queue Time: " + getElapsetTime(jobBuildMetrics.getQueueDate(), jobBuildMetrics.getStartDate())
                    + "<br/>Execution Time: " + getElapsetTime(jobBuildMetrics.getStartDate(), jobBuildMetrics.getFinishDate())
                    + "<br/>Duration: " + getElapsetTime(jobBuildMetrics.getQueueDate(), jobBuildMetrics.getFinishDate())
                    + "</pre>";

            js.append("[\"")
                    .append(jobBuildMetrics.getJob().getName()).append("\",")
                    .append("\"Queue\",\"")
                    .append(tooltip).append("\",\"")
                    .append(sdf.format(jobBuildMetrics.getQueueDate())).append("\",\"")
                    .append(sdf.format(jobBuildMetrics.getStartDate())).append("\"")
                    .append("],");

            js.append("[\"")
                    .append(jobBuildMetrics.getJob().getName()).append("\",")
                    .append("\"Proccess\",\"")
                    .append(tooltip).append("\",\"")
                    .append(sdf.format(jobBuildMetrics.getStartDate())).append("\",\"")
                    .append(sdf.format(jobBuildMetrics.getFinishDate())).append("\"")
                    .append("]");
            if (index != metricsSize) {
                js.append(",");
            }
            index++;
        }
        js.append("]");

        return js.toString();
    }
}

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
package br.com.dafiti.hanger.scheduler;

import br.com.dafiti.hanger.controller.JobController;
import br.com.dafiti.hanger.model.JobTrigger;
import br.com.dafiti.hanger.option.Priority;
import br.com.dafiti.hanger.service.JobService;
import br.com.dafiti.hanger.service.JobTriggerService;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author Helio Leal
 */
@Component
public class JobSpark implements Job {

    private final JobController jobController;
    private final JobTriggerService jobTriggerService;

    private static final Logger LOG = LogManager.getLogger(JobSpark.class.getName());

    @Autowired
    public JobSpark(
            JobController jobController,
            JobService jobService,
            JobTriggerService jobTriggerService) {
        this.jobController = jobController;
        this.jobTriggerService = jobTriggerService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        UUID uuid = UUID.randomUUID();
        String triggerName = context.getTrigger().getKey().getName();

        LOG.log(Level.INFO, "[" + uuid + "] Trigger " + triggerName + " sparked");

        List<JobTrigger> jobTriggers = this.jobTriggerService.findByTriggerNameOrderByPriorityDesc(triggerName);

        for (JobTrigger jobTrigger : jobTriggers) {
            if (jobTrigger.isEnabled()) {
                LOG.log(Level.INFO, "[" + uuid + "] [Priority: "+ Priority.of(jobTrigger.getPriority())+"] Job " + jobTrigger.getJob().getName() + " sparked by trigger " + triggerName);
                this.jobController.build(jobTrigger.getJob());
            }
        }
    }
}

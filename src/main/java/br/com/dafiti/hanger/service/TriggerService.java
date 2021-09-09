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
package br.com.dafiti.hanger.service;

import br.com.dafiti.hanger.model.TriggerDetail;
import br.com.dafiti.hanger.scheduler.JobSpark;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

/**
 *
 * @author Helio Leal
 */
@Service
public class TriggerService {

    private final Scheduler scheduler;
    private final JobTriggerService jobTriggerService;

    @Autowired
    public TriggerService(
            Scheduler scheduler,
            JobTriggerService jobTriggerService) {
        this.scheduler = scheduler;
        this.jobTriggerService = jobTriggerService;
    }

    @Cacheable(value = "triggers")
    public Iterable<TriggerDetail> list() {
        List<TriggerDetail> triggerDetails = new ArrayList();

        try {
            for (String group : this.scheduler.getTriggerGroupNames()) {
                for (TriggerKey triggerKey : this.scheduler.getTriggerKeys(groupEquals(group))) {
                    CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);

                    TriggerDetail triggerDetail = new TriggerDetail();
                    triggerDetail.setName(triggerKey.getName());
                    triggerDetail.setCron(cronTrigger.getCronExpression());
                    triggerDetail.setDescription(cronTrigger.getDescription());

                    triggerDetails.add(triggerDetail);
                }

            }
        } catch (SchedulerException ex) {
            Logger.getLogger(TriggerService.class.getName()).log(Level.SEVERE, null, ex);
        }

        return triggerDetails;
    }

    /**
     * Inserts a trigger.
     *
     * @param triggerDetail
     * @throws Exception
     */
    @Caching(evict = {
        @CacheEvict(value = "triggers", allEntries = true)})
    public void save(TriggerDetail triggerDetail) throws Exception {
        this.jobTriggerService.deleteAndSaveAll(triggerDetail.getJobTriggers());
        this.scheduler.scheduleJob(this.buildTrigger(triggerDetail));
    }

    /**
     * Unshedule a trigger.
     *
     * @param triggerName String
     * @throws Exception
     */
    @Caching(evict = {
        @CacheEvict(value = "triggers", allEntries = true)})
    public void delete(String triggerName) throws Exception {
        try {
            this.scheduler.unscheduleJob(triggerKey(triggerName));
        } catch (SchedulerException ex) {
            throw new Exception("Error on unscheduling trigger: " + ex.getMessage());
        }
    }

    /**
     * Gets a trigger
     *
     * @param triggerName String
     * @return TriggerDetail
     * @throws Exception
     */
    public TriggerDetail get(String triggerName) throws Exception {
        TriggerDetail triggerDetail = new TriggerDetail();

        for (String group : this.scheduler.getTriggerGroupNames()) {
            for (TriggerKey triggerKey : this.scheduler.getTriggerKeys(groupEquals(group))) {
                if (triggerKey.getName().trim().toLowerCase().equals(triggerName.trim().toLowerCase())) {
                    CronTrigger cronTrigger = (CronTrigger) scheduler.getTrigger(triggerKey);
                    triggerDetail.setName(triggerKey.getName());
                    triggerDetail.setCron(cronTrigger.getCronExpression());
                    triggerDetail.setDescription(cronTrigger.getDescription());
                    triggerDetail.setJobTriggers(this.jobTriggerService.findByTriggerName(triggerKey.getName().trim()));
                    break;
                }
            }
        }

        return triggerDetail;
    }

    /**
     * Update existing trigger
     *
     * @param triggerDetail TriggerDetail
     * @throws Exception
     */
    @Caching(evict = {
        @CacheEvict(value = "triggers", allEntries = true)})
    public void update(TriggerDetail triggerDetail) throws Exception {
        this.jobTriggerService.deleteAndSaveAll(triggerDetail.getJobTriggers());
        scheduler.rescheduleJob(triggerKey(triggerDetail.getName()), this.buildTrigger(triggerDetail));
    }

    /**
     * Prepare trigger setup.
     *
     * @param triggerDetail
     * @return
     */
    private Trigger buildTrigger(TriggerDetail triggerDetail) {
        return TriggerBuilder.newTrigger()
                .forJob(this.buildJobDetail())
                .withIdentity(triggerDetail.getName())
                .withDescription(triggerDetail.getDescription())
                .withSchedule(CronScheduleBuilder.cronSchedule(triggerDetail.getCron()))
                .build();
    }

    /**
     * Prepare job detail setup.
     *
     * @return
     */
    private JobDetail buildJobDetail() {
        return JobBuilder
                .newJob()
                .ofType(JobSpark.class)
                .storeDurably()
                .withIdentity("JobSpark")
                .withDescription("JobSpark is responsible to fire a list of hanger jobs.")
                .build();
    }

    /**
     * Create job details if not exists.
     */
    public void createJobDetailsIfNotExists() {
        try {
            if (!this.scheduler.checkExists(JobKey.jobKey("JobSpark"))) {
                this.scheduler.addJob(this.buildJobDetail(), true);
            }
        } catch (SchedulerException ex) {
            Logger.getLogger(TriggerService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

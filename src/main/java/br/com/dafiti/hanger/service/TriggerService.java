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
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
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

    @Autowired
    public TriggerService(Scheduler scheduler) {
        this.scheduler = scheduler;
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

    @Caching(evict = {
        @CacheEvict(value = "triggers", allEntries = true)})
    public void save(TriggerDetail triggerDetail) throws Exception {

        JobDetail jobDetail = JobBuilder
                .newJob()
                .ofType(JobSpark.class)
                .storeDurably()
                .withIdentity("sampleJobSpark")
                .withDescription("description JobSpark")
                .build();

        JobDataMap jobDataMap = new JobDataMap();

        triggerDetail.getJobs().forEach((job) -> {
            jobDataMap.put(String.valueOf(job.getId()), job.getName());
        });

        Trigger trigger = TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(triggerDetail.getName())
                .withDescription(triggerDetail.getDescription())
                .withSchedule(CronScheduleBuilder.cronSchedule(triggerDetail.getCron()))
                .usingJobData(jobDataMap)
                .build();

        try {
            scheduler.scheduleJob(trigger).toString();
        } catch (SchedulerException ex) {
            throw new Exception("Error on scheduling: " + ex.getMessage());
        }
    }

    @Caching(evict = {
        @CacheEvict(value = "triggers", allEntries = true)})
    public void delete(String triggerName) throws Exception {
        try {
            this.scheduler.unscheduleJob(triggerKey(triggerName));
        } catch (SchedulerException ex) {
            throw new Exception("Error on unscheduling trigger: " + ex.getMessage());
        }
    }
}

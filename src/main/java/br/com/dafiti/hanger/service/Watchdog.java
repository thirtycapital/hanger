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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import br.com.dafiti.hanger.model.Job;
import br.com.dafiti.hanger.model.JobBuild;
import br.com.dafiti.hanger.model.JobStatus;
import br.com.dafiti.hanger.option.Scope;
import br.com.dafiti.hanger.option.Status;
import br.com.dafiti.hanger.service.JobBuildPushService.PushInfo;

/**
 *
 * @author Valdiney V GOMES
 */
@Service
public class Watchdog {

    private final JobService jobService;
    private final JobDetailsService jobDetailsService;
    private final JobBuildService jobBuildService;
    private final JobBuildPushService jobBuildPushService;
    private final SlackService slackService;
    private final ConnectionService connectionService;
    private final JenkinsService jenkinsServive;

    @Autowired
    public Watchdog(
            JobService jobService,
            JobDetailsService jobDetailsService,
            JobBuildService jobBuildService,
            JobBuildPushService jobBuildPushService,
            SlackService slackService,
            ConnectionService connectionService,
            JenkinsService jenkinsServive) {

        this.jobService = jobService;
        this.jobDetailsService = jobDetailsService;
        this.jobBuildService = jobBuildService;
        this.jobBuildPushService = jobBuildPushService;
        this.slackService = slackService;
        this.connectionService = connectionService;
        this.jenkinsServive = jenkinsServive;
    }

    /**
     * Watchdog patrols at every minute past every 30th hour.
     */
    @Scheduled(cron = "5 */30 * * * *")
    public void patrol() {
        Logger.getLogger(
                Watchdog.class.getName())
                .log(Level.INFO, "The watchdog is patrolling jobs!");

        jobPatrol();

        Logger.getLogger(
                Watchdog.class.getName())
                .log(Level.INFO, "The watchdog is patrolling connections!");

        connectionPatrol();

        Logger.getLogger(
                Watchdog.class.getName())
                .log(Level.INFO, "The watchdog patrol is finished!");
    }

    /**
     * Find jobs that can be built.
     */
    private void jobPatrol() {
        Iterable<Job> jobs = jobService.list();

        for (Job job : jobs) {

            //Identify job waiting forever. 
            if (jobDetailsService.getDetailsOf(job).getStatus().equals(Status.WAITING)) {
                PushInfo push = jobBuildPushService.getPushInfo(job);

                if (push.isReady()) {
                    boolean onlyOptionalorDisabled = job
                            .getParent()
                            .stream()
                            .filter(jobParent -> !jobParent.getScope().equals(Scope.OPTIONAL) && jobParent.getJob().isEnabled())
                            .collect(Collectors.toList())
                            .isEmpty();

                    if (!onlyOptionalorDisabled) {
                        this.catcher(job);
                    }
                }
            }

            //Identify job running forever.
            if (jobDetailsService.getDetailsOf(job).getStatus().equals(Status.RUNNING)) {
                JobStatus jobStatus = job.getStatus();

                if (jobStatus != null) {
                    JobBuild jobBuild = jobStatus.getBuild();

                    if (jobBuild != null) {
                        if (!jenkinsServive.isBuilding(job, jobBuild.getNumber())) {
                            this.catcher(job);
                        }
                    }
                }
            }
        }
    }

    /**
     * Catch a lost job.
     *
     * @param job job
     */
    private void catcher(Job job) {
        StringBuilder message = new StringBuilder();

        try {
            jobBuildService.build(job);

            Logger.getLogger(
                    Watchdog.class.getName())
                    .log(Level.INFO, "The watchdog catched job ".concat(job.getName()));
            message
                    .append(":dog: The watchdog catched job ")
                    .append("*")
                    .append(job.getDisplayName())
                    .append("*");
        } catch (Exception ex) {
            Logger.getLogger(
                    Watchdog.class.getName())
                    .log(Level.SEVERE, "The watchdog fail building " + job.getName(), ex);

            message
                    .append(":hotdog: The watchdog fail building ")
                    .append("*")
                    .append(job.getDisplayName())
                    .append("*");
        }

        if (message.length() != 0) {
            slackService.send(message.toString());
        }
    }

    /**
     * Find broken connections.
     */
    private void connectionPatrol() {
        this.connectionService.listConnectionStatus().forEach((status) -> {
            StringBuilder message = new StringBuilder();
            if (status.getStatus().equals(Status.FAILURE)) {
                message
                        .append(":broken_heart: The connection ")
                        .append(status.getConnection().getName())
                        .append(" is broken!");

                slackService.send(message.toString());
            }
        });
    }
}

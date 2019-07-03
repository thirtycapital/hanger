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
import br.com.dafiti.hanger.model.JobBuild;
import br.com.dafiti.hanger.model.JobStatus;
import br.com.dafiti.hanger.option.Flow;
import br.com.dafiti.hanger.option.Phase;
import br.com.dafiti.hanger.option.Scope;
import br.com.dafiti.hanger.option.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Valdiney V GOMES
 */
@Service
public class EyeService {

    private final JobService jobService;
    private final JobStatusService jobStatusService;
    private final JobBuildService jobBuildService;
    private final JobBuildPushService jobBuildPushService;
    private final JobCheckupService jobCheckupService;
    private final JobNotificationService jobNotificationService;

    @Autowired
    public EyeService(
            JobService jobService,
            JobStatusService jobStatusService,
            JobBuildService jobBuildService,
            JobBuildPushService jobBuildPushService,
            JobCheckupService jobCheckupService,
            SlackService slackService,
            JobNotificationService jobNotificationService) {

        this.jobService = jobService;
        this.jobStatusService = jobStatusService;
        this.jobBuildService = jobBuildService;
        this.jobBuildPushService = jobBuildPushService;
        this.jobCheckupService = jobCheckupService;
        this.jobNotificationService = jobNotificationService;
    }

    /**
     * Hanger eyes that sees everything :).
     *
     * @param notificationPayload Notification Plugin information
     */
    @Async
    @Transactional
    public void observer(String notificationPayload) {
        JSONObject notification = new JSONObject(notificationPayload);
        JSONObject buildNotification = notification.getJSONObject("build");

        //Identify if the job is observed.
        Job job = jobService.findByName(notification.getString("name"));

        if (job != null) {
            boolean checkup = false;
            boolean updateStatus = true;
            String buildStatus = buildNotification.optString("status");
            
            // Display notificationPayload on console.
            Logger.getLogger(EyeService.class.getName()).log(Level.INFO, "{0}: {1}", new Object[]{job.getName(), notificationPayload});

            //Define the job build.
            JobBuild jobBuild = new JobBuild();

            jobBuild.setNumber(buildNotification.getInt("number"));
            jobBuild.setPhase(Phase.valueOf(buildNotification.optString("phase")));
            jobBuild.setDate(buildNotification.getLong("timestamp"));
            jobBuild.setStatus(buildStatus.isEmpty() ? Status.SUCCESS : Status.valueOf(buildNotification.optString("status")));
            jobBuild.setJob(job);

            //Define the job status.
            JobStatus jobStatus = job.getStatus();

            if (jobStatus == null) {
                jobStatus = new JobStatus();
                jobStatus.setScope(Scope.FULL);
                jobStatus.setFlow(Flow.NORMAL);
            }

            switch (jobBuild.getPhase()) {
                case QUEUED:
                    updateStatus = false;
                    break;
                case STARTED:
                    updateStatus = true;
                    break;
                case COMPLETED:
                    updateStatus = false;
                    break;
                case FINALIZED:
                    checkup = true;
                    updateStatus = true;
                    break;
                default:
                    updateStatus = false;
                    break;
            }

            //Save the build.
            jobBuild = jobBuildService.save(jobBuild);

            //Add the trigger and status to the job. 
            if (updateStatus) {
                jobStatus.setDate(new Date());
                jobStatus.setBuild(jobBuild);
                jobStatus.setScope(jobStatus.getScope() == null ? Scope.FULL : jobStatus.getScope());

                //Identify job flow.
                Flow flow = jobStatus.getFlow();

                if (flow == null) {
                    jobStatus.setFlow(Flow.NORMAL);
                } else if (jobBuild.getPhase().equals(Phase.FINALIZED) && jobBuild.getStatus().equals(Status.SUCCESS)) {
                    //Identify if should query the job result.
                    if (checkup) {
                        //Evaluate job checkup.
                        if (jobCheckupService.evaluate(job, jobStatus.getScope())) {
                            jobStatus.setFlow(Flow.NORMAL);
                        } else {
                            jobStatus.setFlow(Flow.UNHEALTHY);
                        }
                    }
                } else {
                    //Identify job as transiente.
                    jobStatus.setFlow(Flow.TRANSIENT);
                }

                //Save the job status.
                jobStatus = jobStatusService.save(jobStatus);

                //Link job status with job.
                job.setStatus(jobStatus);

                //Update the job.
                jobService.save(job);

                //Publish a job notification.
                jobNotificationService.notify(job, true);

                //Identify if the job is finalized sucessfully. 
                if (jobStatus.getFlow().equals(Flow.NORMAL)
                        && jobBuild.getPhase().equals(Phase.FINALIZED)
                        && jobBuild.getStatus().equals(Status.SUCCESS)) {

                    //Push all jobs dependents on a job build. 
                    jobBuildPushService.push(job);
                }
            }
        }
    }
}

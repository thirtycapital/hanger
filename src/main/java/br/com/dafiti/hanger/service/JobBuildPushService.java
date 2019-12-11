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
import br.com.dafiti.hanger.model.JobParent;
import br.com.dafiti.hanger.model.JobStatus;
import br.com.dafiti.hanger.option.Flow;
import br.com.dafiti.hanger.option.Scope;
import br.com.dafiti.hanger.service.JobBuildService.BuildInfo;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Valdiney V GOMES
 */
@Service
public class JobBuildPushService {

    private final JobBuildService jobBuildService;
    private final JobBuildStatusService jobBuildStatusService;
    private final JobParentService jobParentService;
    private final JobService jobService;
    private final JobStatusService jobStatusService;
    private final JobNotificationService jobNotificationService;

    @Autowired
    public JobBuildPushService(
            JobBuildService jobBuildService,
            JobBuildStatusService jobBuildStatusService,
            JobParentService jobParentService,
            JobService jobService,
            JobStatusService jobStatusService,
            JobNotificationService jobNotificationService) {

        this.jobBuildService = jobBuildService;
        this.jobBuildStatusService = jobBuildStatusService;
        this.jobParentService = jobParentService;
        this.jobService = jobService;
        this.jobStatusService = jobStatusService;
        this.jobNotificationService = jobNotificationService;
    }

    /**
     * Push child job build.
     *
     * @param job Job.
     */
    public void push(Job job) {
        HashSet<JobParent> childs = jobParentService.findByParent(job);

        //For each child.
        childs.forEach((child) -> {
            //Get the  child.
            Job childJob = child.getJob();

            //Get job build push information about a job.
            PushInfo push = this.getPushInfo(childJob);

            //Identify if should push the child build.
            if (push.isReady()) {
                JobStatus childJobStatus = childJob.getStatus();

                //Define the job status.
                if (childJobStatus == null) {
                    childJobStatus = new JobStatus();
                }

                //Identify the job scope.
                childJobStatus.setScope(push.getScope());

                //Identify the job status date. 
                childJobStatus.setDate(new Date());

                //Build the job child. 
                try {
                    //Set child job as rebuilding.
                    childJobStatus.setFlow(Flow.REBUILD);
                    childJobStatus = jobStatusService.save(childJobStatus);
                    childJob.setStatus(childJobStatus);
                    jobService.save(childJob);

                    //Build the job.
                    BuildInfo buildInfo = jobBuildService.build(childJob);

                    //Identify if the prevalidation was sucessfuly.
                    if (!buildInfo.isHealthy()) {
                        //Set child job as blocked in case pre-validation fail. 
                        childJobStatus.setFlow(Flow.BLOCKED);
                        childJobStatus = jobStatusService.save(childJobStatus);
                        childJob.setStatus(childJobStatus);
                        jobService.save(childJob);

                        //Publish a job notification.
                        jobNotificationService.notify(childJob, true);
                    }
                } catch (URISyntaxException | IOException ex) {
                    //Set child job build as fail in case of error. 
                    childJobStatus.setFlow(Flow.ERROR);
                    childJobStatus = jobStatusService.save(childJobStatus);
                    childJob.setStatus(childJobStatus);
                    jobService.save(childJob);

                    //Publish a job notification.
                    jobNotificationService.notify(childJob, true);

                    //Fail log.
                    Logger.getLogger(EyeService.class.getName())
                            .log(Level.SEVERE, "Fail building job: " + childJob.getName(), ex);
                }
            }
        });
    }

    /**
     * Identify if can push a job build.
     *
     * @param job Job
     * @return PushInfo Job build push information.
     */
    public PushInfo getPushInfo(Job job) {
        Scope scope = null;
        boolean ready = false;
        Map<Job, Boolean> all = new HashMap();
        Map<Job, Boolean> partial = new HashMap();

        //Identify the job is getPushInfo.
        boolean built = jobBuildStatusService.isBuildable(job);

        if (built) {
            //Get the job parents.
            List<JobParent> parents = job.getParent();

            for (JobParent parent : parents) {
                //Identify if the parent is optional. 
                built = parent.getScope().equals(Scope.OPTIONAL);

                //Identify if the parent is built.
                if (!built) {
                    built = jobBuildStatusService.isBuilt(parent.getParent());

                    if (!built) {
                        //Identify if the parent is disabled.
                        built = !(parent.getParent().isEnabled());
                    }
                }

                //Record all parent build status.
                all.put(parent.getParent(), built);

                //Record partial dependencies build status.
                if (parent.getScope().equals(Scope.PARTIAL)) {
                    partial.put(parent.getParent(), built);
                }
            }

            //Identify if there are only healthy dependencies. 
            boolean full = partial.isEmpty();

            if (full) {
                //Identify if healthy dependencies was built successfully. 
                for (Map.Entry<Job, Boolean> entry : all.entrySet()) {
                    ready = entry.getValue();

                    if (!ready) {
                        break;
                    }
                }

                //Identify the scope.
                if (ready) {
                    scope = Scope.FULL;
                }

                //Log the full dependencies status.
                Logger.getLogger(JobBuildPushService.class.getName()).log(Level.INFO, "Job {0} push: Path FULL, Scope {1}, Parent {2}", new Object[]{job.getName(), scope, all});
            } else {
                //Identify if all dependencies was built successfully.
                for (Map.Entry<Job, Boolean> entry : all.entrySet()) {
                    full = entry.getValue();

                    if (!full) {
                        break;
                    }
                }

                if (full) {
                    ready = true;
                    scope = Scope.FULL;
                } else {
                    //Identify if partial dependencies was built successfully. 
                    for (Map.Entry<Job, Boolean> entry : partial.entrySet()) {
                        ready = entry.getValue();

                        if (!ready) {
                            break;
                        }
                    }

                    //Identify the scope.
                    if (ready) {
                        scope = Scope.PARTIAL;
                    }
                }

                //Log the partial dependencies status.
                Logger.getLogger(JobBuildPushService.class.getName()).log(Level.INFO, "Job {0} push: Path PARTIAL, Scope {1}, Parent {2}, Partial Parent {3}", new Object[]{job.getName(), scope, all, partial});
            }
        } else {
            Logger.getLogger(JobBuildPushService.class.getName()).log(Level.INFO, "Job {0} is not buildable", new Object[]{job.getName()});
        }

        return new PushInfo(ready, scope);
    }

    /**
     * Push information.
     */
    public class PushInfo {

        private boolean ready;
        private Scope scope;

        public PushInfo(boolean ready, Scope scope) {
            this.ready = ready;
            this.scope = scope;
        }

        public boolean isReady() {
            return ready;
        }

        public void setReady(boolean ready) {
            this.ready = ready;
        }

        public Scope getScope() {
            return scope;
        }

        public void setScope(Scope scope) {
            this.scope = scope;
        }
    }
}

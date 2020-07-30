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
import br.com.dafiti.hanger.model.Server;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.model.Build;
import com.offbytwo.jenkins.model.BuildWithDetails;
import com.offbytwo.jenkins.model.JobWithDetails;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 *
 * @author Valdiney V GOMES
 */
@Service
public class JenkinsService {

    private final HttpServletRequest request;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public JenkinsService(
            HttpServletRequest request,
            ApplicationEventPublisher applicationEventPublisher) {

        this.request = request;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * Get a Jenkins server connection.
     *
     * @param server Server Server
     * @return Server connection
     * @throws URISyntaxException
     */
    @Cacheable(value = "jenkinsserver", key = "#server.id")
    public JenkinsServer getJenkinsServer(Server server) throws URISyntaxException {
        JenkinsServer jenkins = null;

        if (server != null) {
            jenkins = new JenkinsServer(new URI(
                    server.getUrl()),
                    server.getUsername(),
                    server.getToken());
        }

        return jenkins;
    }

    /**
     * Test if a server is running.
     *
     * @param server Server Server
     * @return Identify if a job is running.
     */
    public boolean isRunning(Server server) {
        boolean running = false;
        JenkinsServer jenkins;
        try {
            jenkins = this.getJenkinsServer(server);

            if (jenkins != null) {
                running = (jenkins.isRunning());
                jenkins.close();
            }
        } catch (URISyntaxException ex) {
            LogManager.getLogger(JenkinsService.class).log(Level.ERROR, "Fail checking if a server is running!", ex);
        }

        return running;
    }

    /**
     * Get Jenkins job list.
     *
     * @param server Server
     * @return Job list
     * @throws URISyntaxException
     * @throws IOException
     */
    @Cacheable(value = "serverJobs", key = "#server.id")
    public List<String> listJob(Server server) throws URISyntaxException, IOException {
        List<String> jobs = new ArrayList();
        JenkinsServer jenkins = this.getJenkinsServer(server);

        if (jenkins != null) {
            if (jenkins.isRunning()) {
                for (String job : jenkins.getJobs().keySet()) {
                    jobs.add(job);
                }
            }

            jenkins.close();
        }

        return jobs;
    }

    /**
     * Identify if a job is buildable.
     *
     * @param job Job
     * @return Identify if job was built.
     * @throws URISyntaxException
     * @throws IOException
     */
    public boolean isBuildable(Job job) throws URISyntaxException, IOException {
        return this.isBuildable(job.getName(), job.getServer());
    }

    /**
     * Identify if a job is buildable.
     *
     * @param name Job name
     * @param server Server
     * @return Identify if job was built.
     * @throws URISyntaxException
     * @throws IOException
     */
    public boolean isBuildable(String name, Server server) throws URISyntaxException, IOException {
        JenkinsServer jenkins;
        boolean isBuildable = false;

        if (name != null && server != null) {
            jenkins = this.getJenkinsServer(server);

            if (jenkins != null) {
                if (jenkins.isRunning()) {
                    JobWithDetails jobWithDetails = jenkins.getJob(name);

                    if (jobWithDetails != null) {
                        isBuildable = (jobWithDetails.isBuildable());
                    }

                    jenkins.close();
                }
            }
        }

        return isBuildable;
    }

    /**
     * Build a Jenkins job.
     *
     * @param job Job
     * @return Identify if job was built.
     * @throws URISyntaxException
     * @throws IOException
     */
    public boolean build(Job job) throws Exception {
        JenkinsServer jenkins;
        boolean built = false;

        if (job != null) {
            jenkins = this.getJenkinsServer(job.getServer());

            if (jenkins != null) {
                if (jenkins.isRunning()) {
                    JobWithDetails jobWithDetails = jenkins.getJob(job.getName());

                    if (jobWithDetails != null) {
                        try {
                            built = (jobWithDetails.build(true) != null);
                        } catch (IOException ex) {
                            LogManager.getLogger(JenkinsService.class).log(Level.ERROR, "Fail building job: " + job.getName(), ex);
                            try {
                                built = (jobWithDetails.build(new HashMap(), true) != null);
                            } catch (IOException e) {
                                LogManager.getLogger(JenkinsService.class).log(Level.ERROR, "Fail building parametrized job: " + job.getName(), ex);
                                throw ex;
                            }
                        } finally {
                            jenkins.close();
                        }
                    }
                }
            }
        }

        String userName = "";

        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication != null) {
            userName = authentication.getName();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("job", job.getName());
        data.put("status", String.valueOf(built));

        applicationEventPublisher.publishEvent(
                new AuditApplicationEvent(
                        new AuditEvent(userName, "BUILD", data)
                )
        );

        return built;
    }

    /**
     * Get a Jenkins job upstream project list.
     *
     * @param job Job
     * @return Upstream project list
     */
    public List<String> getUpstreamProjects(Job job) {
        JenkinsServer jenkins;
        List<String> upstreamProjects = new ArrayList();

        if (job != null) {
            try {
                jenkins = this.getJenkinsServer(job.getServer());

                if (jenkins != null) {
                    if (jenkins.isRunning()) {
                        JobWithDetails jobWithDetails = jenkins.getJob(job.getName());

                        if (jobWithDetails != null) {
                            jobWithDetails.getUpstreamProjects().stream().forEach((upstream) -> {
                                upstreamProjects.add(upstream.getName());
                            });
                        }
                    }

                    jenkins.close();
                }
            } catch (URISyntaxException | IOException ex) {
                LogManager.getLogger(JenkinsService.class).log(Level.ERROR, "Fail getting upstream projects!", ex);
            }
        }

        return upstreamProjects;
    }

    /**
     * Identify if a job is in queue.
     *
     * @param job Job
     * @return Identify if a job is in queue
     */
    public boolean isInQueue(Job job) {
        JenkinsServer jenkins;
        boolean isInQueue = false;

        if (job != null) {
            try {
                jenkins = this.getJenkinsServer(job.getServer());

                if (jenkins != null) {
                    if (jenkins.isRunning()) {
                        JobWithDetails jobWithDetails = jenkins.getJob(job.getName());

                        if (jobWithDetails != null) {
                            isInQueue = jobWithDetails.isInQueue();
                        }
                    }

                    jenkins.close();
                }
            } catch (IOException | URISyntaxException ex) {
                LogManager.getLogger(JenkinsService.class).log(Level.ERROR, "Fail identifying if a job is in queue!", ex);
            }
        }

        return isInQueue;
    }

    /**
     * Identify if a job is building.
     *
     * @param job Job
     * @param number Job build number
     * @return Identify if a job is building
     */
    public boolean isBuilding(Job job, int number) {
        JenkinsServer jenkins;
        boolean isBuilding = false;

        if (job != null) {
            try {
                jenkins = this.getJenkinsServer(job.getServer());

                if (jenkins != null) {
                    if (jenkins.isRunning()) {
                        JobWithDetails jobWithDetails = jenkins.getJob(job.getName());

                        if (jobWithDetails != null) {
                            //Identifies if the job is in queue. 
                            isBuilding = jobWithDetails.isInQueue();

                            if (!isBuilding) {
                                Build build = jobWithDetails.getBuildByNumber(number);

                                if (build != null) {
                                    BuildWithDetails buildWithDetails = build.details();

                                    if (buildWithDetails != null) {
                                        //Identifies if the job is running. 
                                        isBuilding = buildWithDetails.isBuilding();
                                    }
                                }
                            }
                        }
                    }

                    jenkins.close();
                }
            } catch (IOException | URISyntaxException ex) {
                LogManager.getLogger(JenkinsService.class).log(Level.ERROR, "Fail identifying if a job is building!", ex);
            }
        }

        return isBuilding;
    }

    /**
     * Rename a job.
     *
     * @param job Job
     * @param name Name
     */
    public void renameJob(Job job, String name) {
        JenkinsServer jenkins;

        if (job != null) {
            if (!name.isEmpty() && !job.getName().equals(name)) {
                try {
                    jenkins = this.getJenkinsServer(job.getServer());

                    if (jenkins != null) {
                        if (jenkins.isRunning()) {
                            jenkins.renameJob(name, job.getName(), true);
                        }

                        jenkins.close();
                    }
                } catch (URISyntaxException | IOException ex) {
                    LogManager.getLogger(JenkinsService.class).log(Level.ERROR, "Fail renaming a job!", ex);
                }
            }
        }
    }

    /**
     * Add the Hanger endpoint to the notificion plugin configuration of a
     * Jenkins job.
     *
     * @param job Job
     */
    public void updateJob(Job job) {
        JenkinsServer jenkins;

        if (job != null) {
            try {
                jenkins = this.getJenkinsServer(job.getServer());

                if (jenkins != null) {
                    if (jenkins.isRunning()) {
                        String name = job.getName();
                        String config = jenkins.getJobXml(name);
                        String url = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());

                        //Identify if Notification plugin is configured. 
                        if (!config.contains("com.tikal.hudson.plugins.notification.HudsonNotificationProperty")) {
                            String notificationPluginConfig = ""
                                    + "    <com.tikal.hudson.plugins.notification.HudsonNotificationProperty plugin=\"notification@1.12\">\n"
                                    + "      <endpoints>\n"
                                    + "        <com.tikal.hudson.plugins.notification.Endpoint>\n"
                                    + "          <protocol>HTTP</protocol>\n"
                                    + "          <format>JSON</format>\n"
                                    + "          <urlInfo>\n"
                                    + "            <urlOrId>" + url + "/observer</urlOrId>\n"
                                    + "            <urlType>PUBLIC</urlType>\n"
                                    + "          </urlInfo>\n"
                                    + "          <event>all</event>\n"
                                    + "          <timeout>30000</timeout>\n"
                                    + "          <loglines>0</loglines>\n"
                                    + "          <retries>3</retries>\n"
                                    + "        </com.tikal.hudson.plugins.notification.Endpoint>\n"
                                    + "      </endpoints>\n"
                                    + "    </com.tikal.hudson.plugins.notification.HudsonNotificationProperty>\n";

                            //Add notification plugin tags to Jenkins config XML. 
                            if (config.contains("<properties>")) {
                                config = config.replace("</properties>", notificationPluginConfig + "</properties>");
                            } else {
                                config = config.replace("<properties/>", "<properties>\n" + notificationPluginConfig + "</properties>");
                            }
                        } else if (config.contains("<com.tikal.hudson.plugins.notification.Endpoint>")) {
                            String notificationPluginEndpoint = ""
                                    + "        <com.tikal.hudson.plugins.notification.Endpoint>\n"
                                    + "          <protocol>HTTP</protocol>\n"
                                    + "          <format>JSON</format>\n"
                                    + "          <urlInfo>\n"
                                    + "            <urlOrId>" + url + "/observer</urlOrId>\n"
                                    + "            <urlType>PUBLIC</urlType>\n"
                                    + "          </urlInfo>\n"
                                    + "          <event>all</event>\n"
                                    + "          <timeout>30000</timeout>\n"
                                    + "          <loglines>0</loglines>\n"
                                    + "          <retries>3</retries>\n"
                                    + "        </com.tikal.hudson.plugins.notification.Endpoint>\n";

                            //Replace notification plugin endpoint tag of Jenkins config XML
                            config = config.replaceAll("(?s)<com\\.tikal\\.hudson\\.plugins\\.notification\\.Endpoint>(.*)</com\\.tikal\\.hudson\\.plugins\\.notification\\.Endpoint>", notificationPluginEndpoint);
                        }

                        //Update Jenkins job. 
                        jenkins.updateJob(name, config, true);
                    }

                    jenkins.close();
                }
            } catch (URISyntaxException | IOException ex) {
                LogManager.getLogger(JenkinsService.class).log(Level.WARN, "Fail updating Jenkins job!", ex);
            }
        }
    }

    /**
     * Identify if notification plugin is deployed.
     *
     * @param server Server
     * @return Identify if notification plugin is deployed.
     */
    public boolean hasNotificationPlugin(Server server) {
        boolean notification = false;

        try {
            JenkinsServer jenkins = this.getJenkinsServer(server);

            if (jenkins != null) {
                if (jenkins.isRunning()) {
                    notification = jenkins
                            .getPluginManager()
                            .getPlugins()
                            .stream()
                            .filter(x -> x.getShortName().equals("notification"))
                            .count() == 1;
                }

                jenkins.close();
            }
        } catch (URISyntaxException | IOException ex) {
            LogManager.getLogger(JenkinsService.class).log(Level.ERROR, "Fail getting plugin list!", ex);
        }

        return notification;
    }

    /**
     * Clean the job list cache.
     */
    @Caching(evict = {
        @CacheEvict(value = "serverJobs", allEntries = true)})
    public void refreshCache() {
    }

    /**
     * Identify if a job exists on jenkins.
     *
     * @param job Job
     * @return Identify if a job exists on jenkins.
     */
    public boolean exists(Job job) {
        JenkinsServer jenkins;
        boolean exists = false;

        if (job != null) {
            try {
                jenkins = this.getJenkinsServer(job.getServer());

                if (jenkins != null) {
                    if (jenkins.isRunning()) {
                        JobWithDetails jobWithDetails = jenkins.getJob(job.getName());

                        if (jobWithDetails != null) {
                            exists = true;
                        }
                    }

                    jenkins.close();
                }
            } catch (IOException | URISyntaxException ex) {
                LogManager.getLogger(JenkinsService.class).log(Level.ERROR, "Fail identifying if a job exists!", ex);
            }
        }

        return exists;
    }
}

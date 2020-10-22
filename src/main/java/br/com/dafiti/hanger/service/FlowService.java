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
import br.com.dafiti.hanger.model.JobDetails;
import br.com.dafiti.hanger.model.JobParent;
import br.com.dafiti.hanger.option.Phase;
import br.com.dafiti.hanger.option.Scope;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.P;
import com.hp.gagawa.java.elements.Span;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Valdiney V GOMES
 */
@Service
public class FlowService {

    private Map<String, Step> flow;
    private final JobParentService jobParentService;
    private final HttpServletRequest request;
    private final JobDetailsService jobDetailsService;
    private final JobNotificationService jobNotificationService;

    @Autowired
    public FlowService(
            JobParentService jobParentService,
            HttpServletRequest request,
            JobDetailsService jobDetailsService,
            JobNotificationService jobNotificationService) {

        this.jobParentService = jobParentService;
        this.request = request;
        this.jobDetailsService = jobDetailsService;
        this.jobNotificationService = jobNotificationService;
    }

    /**
     * Define Treant configuration and data.
     *
     * @param job Job
     * @param reverse Identify if is flow or propagation
     * @param expanded Identify if show the flow expanded
     * @return TreantJS script
     */
    public String getJobFlow(
            Job job,
            boolean reverse,
            boolean expanded) {

        List<String> names = new ArrayList();
        List<String> values = new ArrayList();
        StringBuilder js = new StringBuilder();

        //Define Treant configuration. 
        js.append("var config = {");
        js.append("        container: \"#flow\",");
        js.append("        rootOrientation: \"").append(reverse ? "WEST" : "EAST").append("\",");
        js.append("        levelSeparation: 50,");
        js.append("        siblingSeparation: 20,");
        js.append("        nodeAlign: \"LEFT\",");
        js.append("        node: { collapsable: true },");
        js.append("        connectors: {");
        js.append("            type: 'curve'");
        js.append("        }");
        js.append("    }");

        //Get the tree struncture. 
        Map<String, Step> structure = this.getFlowStructure(job, reverse, expanded);

        structure.entrySet().stream().forEach((entry) -> {
            names.add(entry.getValue().getId());
            values.add(entry.getValue().getVariable());
        });

        //Define the tree variable values. 
        js.append(",");
        js.append(String.join(", ", values));

        //Define the tree variables. 
        js.append(",");
        js.append(" chart_config = [");
        js.append("        config,");
        js.append(String.join(",", names));
        js.append("];");

        //Define the tree graph. 
        js.append("new Treant( chart_config );");

        return js.toString();
    }

    /**
     * Get the job flow.
     *
     * @param job Job
     * @param reverse Identify if is flow or propagation
     * @param expanded Identify if show the flow expanded
     * @return Flow structure as a map.
     */
    private Map<String, Step> getFlowStructure(
            Job job,
            boolean reverse,
            boolean expanded) {

        Scope parentScope = null;
        String jobLineage = "";
        String parentLineage = "";

        flow = new TreeMap();

        return getFlowStructure(job, null, false, 0, reverse, expanded, jobLineage, parentLineage, parentScope);
    }

    /**
     * Get the job flow.
     *
     * @param job Job
     * @param parent Job Parent
     * @return Flow structure as a map.
     */
    private Map<String, Step> getFlowStructure(
            Job job,
            Job parent,
            boolean isBlocker,
            int level,
            boolean reverse,
            boolean expanded,
            String jobLineage,
            String parentLineage,
            Scope parentScope) {

        Scope scope = null;

        //Identify the step.
        level++;

        //Identify the lineage.
        jobLineage += "_" + job.getId();

        if (parent != null) {
            parentLineage += "_" + parent.getId();
        }

        //Identify the flow direction.
        if (reverse) {
            HashSet<JobParent> childs = jobParentService.findByParent(job);

            if (!childs.isEmpty()) {
                for (JobParent child : childs) {
                    getFlowStructure(child.getJob(), child.getParent(), child.isBlocker(), level, reverse, expanded, jobLineage, parentLineage, parentScope);
                }
            }
        } else if (!job.getParent().isEmpty()) {
            for (JobParent jobParent : job.getParent()) {
                getFlowStructure(jobParent.getParent(), jobParent.getJob(), jobParent.isBlocker(), level, reverse, expanded, jobLineage, parentLineage, jobParent.getScope());
            }
        }

        //Get the details getDetails each job. 
        JobDetails jobDetails = jobDetailsService.getDetailsOf(job);

        //Define the job checkup link.        
        A jobCheckupLink = new A();

        if (!job.getCheckup().isEmpty()) {
            jobCheckupLink.setHref(request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath()) + "/checkup/job/" + job.getId() + "/list/");
            jobCheckupLink.setTarget("_blank");
            jobCheckupLink.setCSSClass("node-checkup");
            jobCheckupLink.appendText("CHECKUP");
        }

        //Define the job phase link.  
        A jobPhaseLink = new A();

        if (!jobDetails.getPhase().equals(Phase.NONE)) {
            jobPhaseLink.setHref(job.getServer().getUrl() + "job/" + job.getName() + "/" + jobDetails.getBuildNumber() + "/console");
            jobPhaseLink.setTarget("_blank");
            jobPhaseLink.setCSSClass("node-phase");
            jobPhaseLink.appendText(jobDetails.getPhase().toString());
        }

        //Identify optional scope. 
        if (parentScope != null) {
            if (parentScope.equals(Scope.OPTIONAL)) {
                scope = Scope.OPTIONAL;
            } else if (parentScope.equals(Scope.PARTIAL)) {
                scope = Scope.PARTIAL;
            }
        }

        //Define the job name link.  
        A jobNameLink = new A();
        jobNameLink.setHref(request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath()) + "/job/view/" + job.getId());
        jobNameLink.setTarget("_blank");
        jobNameLink.setCSSClass("node-name");
        jobNameLink.appendText(job.getDisplayName());

        //Define the jenkins link. 
        A jobLink = new A();
        jobLink.setHref(job.getServer().getUrl() + "job/" + job.getName());
        jobLink.setTarget("_blank");
        jobLink.setCSSClass("node-link");
        jobLink.appendText("+");

        //Define the job build status span.
        String label = "";

        switch (jobDetails.getStatus()) {
            case REBUILD:
            case RUNNING:
                label = "label label-primary";
                break;
            case SUCCESS:
            case APPROVED:
                label = "label label-success";
                break;
            case ABORTED:
            case FAILURE:
            case UNHEALTHY:
            case ERROR:
            case BLOCKED:
            case DISAPPROVED:
                label = "label label-danger";
                break;
            case DISABLED:
                label = "label label-default";
                break;
            case UNSTABLE:
            case CHECKUP:
                label = "label label-warning";
                break;
            case RESTRICTED:
            case WAITING:
                label = "label label-neutral";
                break;
        }

        Span spanStatus = new Span();
        spanStatus.setCSSClass(label);
        spanStatus.setTitle(jobDetails.getStatus().toString());
        spanStatus.appendText(jobDetails.getStatus().toString());

        P jobStatus = new P();
        jobStatus.appendChild(spanStatus);

        //Define the job build time paragraph.
        P jobBuildTime = new P();
        jobBuildTime.appendText(jobDetails.getBuildTime());
        jobBuildTime.setCSSClass("node-time");

        //Define the node HTML content. 
        StringBuilder node = new StringBuilder();
        node.append(jobNameLink.write());
        node.append(jobLink.write());

        if (jobPhaseLink.getTarget() != null) {
            node.append(jobPhaseLink.write());
        }

        if (jobCheckupLink.getTarget() != null) {
            node.append(jobCheckupLink.write());
        }

        node.append(jobStatus.write());
        node.append(jobBuildTime.write());

        //Identify root step.
        if (parent == null) {
            //Define the job scope paragraph.
            P jobBuildScope = new P();
            jobBuildScope.appendText(jobDetails.getScope() == null ? "" : job.getServer().getName() + " | " + jobDetails.getScope());
            jobBuildScope.setCSSClass("node-scope");

            node.append(jobBuildScope.write());

            flow.put(
                    StringUtils.leftPad(String.valueOf(level), 5, "0"),
                    new Step(
                            jobLineage,
                            jobLineage + " = "
                            + "{"
                            + "     innerHTML: '" + node.toString() + "',"
                            + "     HTMLid: \"" + job.getId() + "\","
                            + "     HTMLclass: \"" + "flow-job-clickable" + "\""
                            + "}")
            );
        } else {
            //Define the job scope paragraph.
            P jobBuildScope = new P();
            jobBuildScope.appendText((scope == null
                    ? (jobDetails.getScope() == null ? "" : job.getServer().getName() + " | " + jobDetails.getScope())
                    : (job.getServer().getName() + " | " + scope.toString() + (job.isRebuild() ? " | REBUILD " + (job.getWait() != 0 ? "once every " + job.getWait() + " min" : "") : ""))) + (parent.isRebuildBlocked() && isBlocker ? " | BLOCKER" : ""));
            jobBuildScope.setCSSClass("node-scope");

            node.append(jobBuildScope.write());

            //Identify child steps.
            flow.put(
                    StringUtils.leftPad(String.valueOf(level), 5, "0")
                    + StringUtils.leftPad(jobLineage, 100, "0")
                    + StringUtils.leftPad(parentLineage, 100, "0"),
                    new Step(
                            jobLineage,
                            jobLineage + " = "
                            + "{ parent: " + parentLineage + ", "
                            + "     innerHTML: '" + node.toString() + "',"
                            + "     HTMLid: \"" + job.getId() + "\","
                            + "     collapsed: " + (job.getParent().isEmpty() || reverse || expanded ? "false" : "true") + ","
                            + "     HTMLclass: \"" + "flow-job-clickable" + "\""
                            + "}")
            );
        }

        return flow;
    }

    /**
     * Get flow warning list.
     *
     * @param job Job
     * @return Warning list
     */
    public List<JobDetails> getFlowWarning(Job job) {
        List<JobDetails> jobDetails = new ArrayList();

        if (jobNotificationService.isNotified(job)) {
            jobDetails = jobDetailsService.getDetailsOf(jobNotificationService.getNotice(job));
        }

        return jobDetails;
    }

    /**
     * Represents a flow step.
     */
    private class Step {

        private String id;
        private String variable;

        public Step(String id, String variable) {
            this.id = id;
            this.variable = variable;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getVariable() {
            return variable;
        }

        public void setVariable(String variable) {
            this.variable = variable;
        }
    }
}

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
 * Icons Pack: Bitsies icons por Bitsies (143 Ícones) Designer: Bitsies
 * Categoria: Aplicação, Objeto, Alimentos Licença: CC Attribution-ShareAlike
 * 3.0 Unported Liconset url: http://www.recepkutuk.com/bitsies/
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

        String phase;
        String checkup;
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

        //Define the checkup object.
        if (job.getCheckup().isEmpty()) {
            checkup = "";
        } else {
            checkup = " checkup: "
                    + " { "
                    + "     val: \"" + "CHECKUP" + "\", "
                    + "     target: \"_blank\", "
                    + "     href: \"" + request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath()) + "/checkup/job/" + job.getId() + "/list/" + "\""
                    + " },";
        }

        //Define the phase object. 
        if (jobDetails.getPhase().equals(Phase.NONE)) {
            phase = "";
        } else {
            phase = " phase: "
                    + " { "
                    + "     val: \"" + jobDetails.getPhase() + "\", "
                    + "     target: \"_blank\", "
                    + "     href: \"" + job.getServer().getUrl() + "job/" + job.getName() + "/" + jobDetails.getBuildNumber() + "/console" + "\""
                    + " },";
        }

        //Identify optional scope. 
        if (parentScope != null) {
            if (parentScope.equals(Scope.OPTIONAL)) {
                scope = Scope.OPTIONAL;
            } else if (parentScope.equals(Scope.PARTIAL)) {
                scope = Scope.PARTIAL;
            }
        }

        //Identify root step.
        if (parent == null) {
            flow.put(
                    StringUtils.leftPad(String.valueOf(level), 5, "0"),
                    new Step(
                            jobLineage,
                            jobLineage + " = "
                            + "{ text:"
                            + "     { name: "
                            + "         {  "
                            + "             val: \"" + job.getDisplayName() + "\", "
                            + "             href: \"" + request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath()) + "/job/view/" + job.getId() + "\" "
                            + "         }, "
                            + "         link: "
                            + "         {  "
                            + "             val: \"" + "+" + "\", "
                            + "             target: \"_blank\", "
                            + "             href: \"" + job.getServer().getUrl() + "job/" + job.getName() + "\" "
                            + "         }, "
                            + phase
                            + checkup
                            + "         server: \"" + job.getServer().getName() + "\", "
                            + "         time: \"" + jobDetails.getBuildTime() + "\", "
                            + "         scope: \"" + (jobDetails.getScope() == null ? "" : jobDetails.getScope()) + "\", "
                            + "     }, "
                            + "     image: \"../../images/" + jobDetails.getStatus() + ".png\", "
                            + "     HTMLid: \"" + job.getId() + "\","
                            + "HTMLclass: \"" + "flow-job-clickable" + "\""
                            + "}")
            );
        } else {
            //Identify child steps.
            flow.put(
                    StringUtils.leftPad(String.valueOf(level), 5, "0")
                    + StringUtils.leftPad(jobLineage, 100, "0")
                    + StringUtils.leftPad(parentLineage, 100, "0"),
                    new Step(
                            jobLineage,
                            jobLineage + " = "
                            + "{ parent: " + parentLineage + ", "
                            + "  text:"
                            + "     { "
                            + "         name: "
                            + "             { "
                            + "                 val: \"" + job.getDisplayName() + "\", "
                            + "                 href: \"" + request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath()) + "/job/view/" + job.getId() + "\""
                            + "             }, "
                            + "         link: "
                            + "         {  "
                            + "             val: \"" + "+" + "\", "
                            + "             target: \"_blank\", "
                            + "             href: \"" + job.getServer().getUrl() + "job/" + job.getName() + "\" "
                            + "         }, "
                            + phase
                            + checkup
                            + "         server: \"" + job.getServer().getName() + "\", "
                            + "         time: \"" + jobDetails.getBuildTime() + "\", "
                            + "         scope: \""
                            + (scope == null
                                    ? (jobDetails.getScope() == null ? "" : jobDetails.getScope())
                                    : (scope + (job.isRebuild() ? ", rebuild" + (job.getWait() != 0 ? " interval: " + job.getWait() + " min" : "") : ""))) + (parent.isRebuildBlocked() && isBlocker ? ", blocker" : "") + "\", "
                            + "     }, "
                            + "     image: \"../../images/" + jobDetails.getStatus() + ".png\","
                            + "collapsed: " + (job.getParent().isEmpty() || reverse || expanded ? "false" : "true") + ","
                            + "HTMLid: \"" + job.getId() + "\","
                            + "HTMLclass: \"" + "flow-job-clickable" + "\""
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

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
package br.com.dafiti.hanger.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Size;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OrderBy;

/**
 *
 * @author Valdiney V GOMES
 */
@Entity
@Table(indexes = {
    @Index(name = "IDX_name", columnList = "name", unique = false)})
public class Job extends Tracker implements Serializable {

    private Long id;
    private Server server;
    private String name;
    private String alias;
    private String description;
    private int retry;
    private int tolerance;
    private int wait;
    private User approver;
    private JobStatus status;
    private List<JobParent> parent = new ArrayList();
    private List<Subject> subject = new ArrayList();
    private List<JobCheckup> checkup = new ArrayList();
    private List<JobApproval> approval = new ArrayList();
    private Set<String> channel = new HashSet();
    private boolean enabled = true;
    private boolean notify;
    private boolean rebuild;
    private boolean rebuildBlocked;

    public Job() {
    }

    public Job(Long id) {
        this.id = id;
    }

    public Job(String name, Server server) {
        this.name = name;
        this.server = server;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @OneToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "server_id", referencedColumnName = "id")
    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @Size(min = 1, max = 255)
    @Column(unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Transient
    public String getDisplayName() {
        if (alias == null || alias.isEmpty()) {
            return name.replaceAll(" ", "_");
        }
        return alias.replaceAll(" ", "_") + "[alias]";
    }

    @Column(columnDefinition = "text")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    public String getHTMLDescription() {
        Parser parser = Parser.builder().build();
        Node document = parser.parse(this.getDescription());
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        return renderer.render(document);
    }

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    public JobStatus getStatus() {
        if (status == null) {
            status = new JobStatus();
        }
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinTable(name = "job_subject",
            joinColumns = {
                @JoinColumn(name = "job_id", referencedColumnName = "id")},
            inverseJoinColumns = {
                @JoinColumn(name = "subject_id", referencedColumnName = "id")})
    public List<Subject> getSubject() {
        return subject;
    }

    public void setSubject(List<Subject> subject) {
        this.subject = subject;
    }

    public void addSubject(Subject subject) {
        this.subject.add(subject);
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    @BatchSize(size = 20)
    @OrderBy(clause = "id, scope")
    public List<JobParent> getParent() {
        return parent;
    }

    public void setParent(List<JobParent> parent) {
        this.parent = parent;
    }

    public void addParent(JobParent parent) {
        this.parent.add(parent);
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    @BatchSize(size = 20)
    public List<JobCheckup> getCheckup() {
        return checkup;
    }

    public void setCheckup(List<JobCheckup> checkup) {
        this.checkup = checkup;
    }

    public void addCheckup(JobCheckup checkup) {
        this.checkup.add(checkup);
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    @BatchSize(size = 20)
    public List<JobApproval> getApproval() {
        return approval;
    }

    public void setApproval(List<JobApproval> approval) {
        this.approval = approval;
    }

    public void addApproval(JobApproval approval) {
        this.approval.add(approval);
    }

    public boolean isRebuild() {
        return rebuild;
    }

    public void setRebuild(boolean rebuild) {
        this.rebuild = rebuild;
    }

    public boolean isNotify() {
        return notify;
    }

    public void setNotify(boolean notify) {
        this.notify = notify;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public int getTolerance() {
        return tolerance;
    }

    public void setTolerance(int tolerance) {
        this.tolerance = tolerance;
    }

    public int getWait() {
        return wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    public User getApprover() {
        return this.approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "job_channel", joinColumns = @JoinColumn(name = "id"))
    @Column(name = "channel")
    public Set<String> getChannel() {
        return channel;
    }

    public void setChannel(Set<String> channel) {
        this.channel = channel;
    }

    public void addChannel(String channel) {
        this.channel.add(channel);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRebuildBlocked() {
        return rebuildBlocked;
    }

    public void setRebuildBlocked(boolean rebuildBlocked) {
        this.rebuildBlocked = rebuildBlocked;
    }

    @Override
    public String toString() {
        StringBuilder job = new StringBuilder();
        StringBuilder jobParent = new StringBuilder();

        for (JobParent parents : this.parent) {
            if (jobParent.length() != 0) {
                jobParent.append(",");
            }

            jobParent.append("{");
            jobParent.append("\"name\":").append(parents.getParent().getName()).append("\",");
            jobParent.append("\"scope\":").append(parents.getScope()).append("\"");
            jobParent.append("}");
        }

        job.append("{\n");
        job.append("    \"job\": {\n");
        job.append("        \"name\":").append("\"").append(this.name).append("\"\n");
        job.append("        \"parent\":").append("[").append(jobParent.toString()).append("]\n");
        job.append("    }\n");
        job.append("}");

        return job.toString();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.id);

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        final Job other = (Job) obj;

        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        return Objects.equals(this.id, other.id);
    }
}

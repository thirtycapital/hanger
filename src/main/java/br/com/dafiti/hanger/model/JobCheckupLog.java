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

import br.com.dafiti.hanger.option.Action;
import br.com.dafiti.hanger.option.Conditional;
import br.com.dafiti.hanger.option.Scope;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author Valdiney V GOMES
 */
@Entity
public class JobCheckupLog implements Serializable {

    private Long id;
    private Date date = new Date();
    private String query;
    private String threshold;
    private Conditional conditional;
    private String value;
    private boolean success;
    private Action action;
    private Scope scope;
    private List<CommandLog> commandLog = new ArrayList();

    public JobCheckupLog() {
    }

    public JobCheckupLog(JobCheckup jobCheckup) {
        this(jobCheckup, "", false);
    }

    public JobCheckupLog(JobCheckup jobCheckup, String value, boolean success) {
        this.query = jobCheckup.getQuery();
        this.threshold = jobCheckup.getThreshold();
        this.conditional = jobCheckup.getConditional();
        this.action = jobCheckup.getAction();
        this.scope = jobCheckup.getScope();
        this.value = value;
        this.success = success;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Column(columnDefinition = "text")
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getThreshold() {
        if (threshold == null) {
            return "";
        } else {
            return threshold.length() > 255 ? threshold.substring(0, 250) : threshold;
        }
    }

    public void setThreshold(String threshold) {
        if (threshold == null) {
            this.threshold = "";
        } else {
            this.threshold = threshold.length() > 255 ? threshold.substring(0, 250) : threshold;
        }
    }

    @Enumerated(EnumType.STRING)
    public Conditional getConditional() {
        return conditional;
    }

    public void setConditional(Conditional conditional) {
        this.conditional = conditional;
    }

    public String getValue() {
        if (value == null) {
            return "";
        } else {
            return value.length() > 255 ? value.substring(0, 250) : value;
        }
    }

    public void setValue(String value) {
        if (value == null) {
            this.value = "";
        } else {
            this.value = value.length() > 255 ? value.substring(0, 250) : value;
        }
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Enumerated(EnumType.STRING)
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "job_checkup_log_command_log",
            joinColumns = {
                @JoinColumn(name = "job_checkup_log_id", referencedColumnName = "id")},
            inverseJoinColumns = {
                @JoinColumn(name = "command_log_id", referencedColumnName = "id")})
    public List<CommandLog> getCommandLog() {
        return commandLog;
    }

    public void setCommandLog(List<CommandLog> commandLog) {
        this.commandLog = commandLog;
    }

    public void addCommandLog(CommandLog commandLog) {
        this.commandLog.add(commandLog);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        final JobCheckupLog other = (JobCheckupLog) obj;

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

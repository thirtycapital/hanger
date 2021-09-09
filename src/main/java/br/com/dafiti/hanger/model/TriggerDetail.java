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
package br.com.dafiti.hanger.model;

import com.cronutils.descriptor.CronDescriptor;
import static com.cronutils.model.CronType.QUARTZ;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.persistence.Transient;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

/**
 *
 * @author Helio Leal
 */
@Component
public class TriggerDetail implements Serializable {

    private String name;
    private String description;
    private String cron;
    private List<JobTrigger> jobTriggers = new ArrayList();

    @NotEmpty
    @Size(min = 5, max = 70)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NotEmpty
    @Size(min = 5, max = 255)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotEmpty
    @Size(min = 13, max = 50)
    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public List<JobTrigger> getJobTriggers() {
        return jobTriggers;
    }

    public void setJobTriggers(List<JobTrigger> jobTriggers) {
        this.jobTriggers = jobTriggers;
    }    

    public void addJobTrigger(JobTrigger jobTrigger) {
        this.jobTriggers.add(jobTrigger);
    }

    @Transient
    public String getCronDescription() {
        String verbose = "";

        if (this.getCron() != null) {
            if (!this.getCron().isEmpty()) {
                verbose = StringUtils.capitalize(
                        CronDescriptor
                                .instance(Locale.ENGLISH)
                                .describe(new CronParser(
                                        CronDefinitionBuilder.instanceDefinitionFor(QUARTZ))
                                        .parse(this.getCron())
                                )
                );
            }
        }

        return verbose;
    }

}

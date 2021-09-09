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

import br.com.dafiti.hanger.model.JobTrigger;
import br.com.dafiti.hanger.repository.JobTriggerRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author Helio Leal
 */
@Service
public class JobTriggerService {

    private final JobTriggerRepository jobTriggerRepository;

    @Autowired
    public JobTriggerService(JobTriggerRepository jobTriggerRepository) {
        this.jobTriggerRepository = jobTriggerRepository;
    }

    void deleteAndSaveAll(List<JobTrigger> jobTriggers) {
        this.jobTriggerRepository.deleteAll(jobTriggers);
        this.jobTriggerRepository.saveAll(jobTriggers);
    }

    public List<JobTrigger> findByTriggerName(String triggerName) {
        return this.jobTriggerRepository.findByTriggerName(triggerName);
    }

}

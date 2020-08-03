/*
 * Copyright (c) 2020 Dafiti Group
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
package br.com.dafiti.hanger.repository;

import br.com.dafiti.hanger.model.Auditor;
import br.com.dafiti.hanger.service.AuditorService;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Valdiney V GOMES
 */
@Repository
public class MyAuditEventRepository {

    @Autowired
    private AuditorService eventAuditorRepository;

    @Bean
    public org.springframework.boot.actuate.audit.AuditEventRepository auditEventRepository() {
        return new org.springframework.boot.actuate.audit.AuditEventRepository() {

            @Override
            public List<AuditEvent> find(String principal, Instant after, String type) {
                Iterable<Auditor> eventAuditor;

                if (principal == null && after == null) {
                    eventAuditor = eventAuditorRepository.list();
                } else if (after == null) {
                    eventAuditor = eventAuditorRepository.listByUsername(principal);
                } else {
                    eventAuditor = eventAuditorRepository.listByUsernameAndDate(principal, Date.from(after));
                }

                return eventAuditorRepository.getAuditEvent(eventAuditor);
            }

            @Async
            @Override
            @Transactional(Transactional.TxType.REQUIRES_NEW)
            public void add(AuditEvent auditEvent) {
                Auditor event = new Auditor();
                event.setUsername(auditEvent.getPrincipal());
                event.setType(auditEvent.getType());
                event.setDate(Date.from(auditEvent.getTimestamp()));
                event.setData(auditEvent.getData());

                eventAuditorRepository.save(event);
            }
        };
    }
}

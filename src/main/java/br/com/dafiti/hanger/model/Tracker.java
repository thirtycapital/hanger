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

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PreRemove;
import javax.persistence.Transient;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 *
 * @author Valdiney V GOMES
 */
@Component
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public class Tracker<T> {

    private Timestamp createdAt;
    private Timestamp updatedAt;
    private String createdBy;
    private String modifiedBy;

    private static ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @CreationTimestamp
    @Column(insertable = true, updatable = false)
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @UpdateTimestamp
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    @CreatedBy
    @Column(insertable = true, updatable = false)
    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    @LastModifiedBy
    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    /**
     * Logs when a record is ADD.
     */
    @PostPersist
    public void postPersist() {
        Map<String, Object> data = new HashMap<>();
        data.put("entity", ((T) this).toString());

        applicationEventPublisher.publishEvent(
                new AuditApplicationEvent(
                        new AuditEvent(
                                this.getLoggedUserName(), "ADD_" + ((T) this).getClass().getSimpleName().toUpperCase(),
                                data
                        )
                )
        );
    }

    /**
     * Logs when a record is UPDATED.
     */
    @PostUpdate
    public void postUpdate() {
        Map<String, Object> data = new HashMap<>();
        data.put("entity", ((T) this).toString());

        applicationEventPublisher.publishEvent(
                new AuditApplicationEvent(
                        new AuditEvent(
                                this.getLoggedUserName(), "UPDATE_" + ((T) this).getClass().getSimpleName().toUpperCase(),
                                data
                        )
                )
        );
    }

    /**
     * Logs when a record is DELETED.
     */
    @PreRemove
    public void PreRemove() {
        Map<String, Object> data = new HashMap<>();
        data.put("entity", ((T) this).toString());

        applicationEventPublisher.publishEvent(
                new AuditApplicationEvent(
                        new AuditEvent(
                                this.getLoggedUserName(), "DELETE_" + ((T) this).getClass().getSimpleName().toUpperCase(),
                                data
                        )
                )
        );
    }

    /**
     * Identifies the current user name.
     *
     * @return Current user name.
     */
    @Transient
    private String getLoggedUserName() {
        String userName = "AnonymousUser";
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (authentication != null) {
            userName = authentication.getName();
        }

        return userName;
    }
}

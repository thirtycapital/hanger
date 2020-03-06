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
package br.com.dafiti.hanger.model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 *
 * @author Helio Leal
 * @author Flávia Lima
 * @author Fernando Saga
 */
@Entity
public class WorkbenchEmail extends Tracker implements Serializable {

    private Long id;
    private String query;
    private String subject;
    private String content;
    private String externalRecipient;
    private List<String> recipient;
    private Connection connection;
    private User user;

    public WorkbenchEmail() {
    }

    public WorkbenchEmail(Connection connection, User user, String subject, String query) {
        this.connection = connection;
        this.user = user;
        this.subject = subject;
        this.query = query;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "workbench_email_recipient", joinColumns = @JoinColumn(name = "workbench_email_id"))
    @Column(name = "recipient")
    public List<String> getRecipient() {
        return recipient;
    }

    public void setRecipient(List<String> recipient) {
        this.recipient = recipient;
    }

    public String getExternalRecipient() {
        return externalRecipient;
    }

    public void setExternalRecipient(String externalRecipient) {
        this.externalRecipient = externalRecipient;
    }

    @Column(columnDefinition = "text")
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query.trim();
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @ManyToOne
    @JoinColumn(name = "connection_id", referencedColumnName = "id")
    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get recipients and external recipients
     *
     * @return
     */
    @Transient
    public Set<String> getAllRecipients() {
        Set<String> recipients = new HashSet();

        this.getRecipient().forEach((r) -> {
            recipients.add(r);
        });

        Arrays.asList(this.getExternalRecipient().split("\\,|\\;")).forEach((e) -> {
            if (!e.isEmpty()) {
                recipients.add(e);
            }
        });

        return recipients;
    }
}

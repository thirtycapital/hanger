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
package br.com.dafiti.hanger.service;

import br.com.dafiti.hanger.model.Blueprint;
import br.com.dafiti.hanger.model.WorkbenchEmail;
import br.com.dafiti.hanger.model.User;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import br.com.dafiti.hanger.repository.WorkbenchEmailRepository;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Fernando Saga
 * @author Helio Leal
 */
@Service
public class WorkbenchEmailService {

    private final ConnectionService connectionService;
    private final MailService mailService;
    private final WorkbenchEmailRepository workbenchEmailRepository;
    private final UserService userService;
    private final ExportService exportService;
    private final ConfigurationService configurationService;

    @Autowired
    public WorkbenchEmailService(
            ConnectionService connectionService,
            WorkbenchEmailRepository workbenchEmailRepository,
            MailService mailService,
            UserService userService,
            ExportService exportService,
            ConfigurationService configurationService) {

        this.connectionService = connectionService;
        this.workbenchEmailRepository = workbenchEmailRepository;
        this.mailService = mailService;
        this.userService = userService;
        this.exportService = exportService;
        this.configurationService = configurationService;
    }

    /**
     * List e-mails.
     *
     * @return
     */
    public Iterable<WorkbenchEmail> list() {
        return workbenchEmailRepository.findAll();
    }

    /**
     * Find user's e-mails.
     *
     * @param user User
     * @return
     */
    public List<WorkbenchEmail> findByUser(User user) {
        return workbenchEmailRepository.findByUser(user);
    }

    /**
     * Save e-mail.
     *
     * @param workbenchEmail WorkbenchEmail
     */
    public void save(WorkbenchEmail workbenchEmail) {
        workbenchEmailRepository.save(workbenchEmail);
    }

    /**
     * Delete a record.
     *
     * @param id Long
     */
    public void delete(Long id) {
        workbenchEmailRepository.deleteById(id);
    }

    /**
     * Export a resultset to e-mail.
     *
     * @param email WorkbenchEmail
     * @param principal Principal
     * @return 
     * @throws java.io.IOException
     */
    public boolean toEmail(WorkbenchEmail email, Principal principal)
            throws IOException, Exception {
        boolean sent = false;
        ConnectionService.QueryResultSet queryResultSet = this.connectionService
                .getQueryResultSet(
                        email.getConnection(),
                        email.getQuery(),
                        principal);

        if (!queryResultSet.hasError()) {
            String fileName = exportService.toCSV(queryResultSet, email.getConnection());

            File file = new File(System.getProperty("java.io.tmpdir")
                    .concat("/")
                    .concat(fileName));

            Blueprint blueprint = new Blueprint(email.getSubject(), "exportQuery");
            blueprint.setFile(file);
            blueprint.addVariable("query", email.getQuery());
            blueprint.addVariable("connection", email.getConnection());
            blueprint.addVariable("content", email.getContent());
            blueprint.addVariable("queryResultSet", queryResultSet);
            blueprint.addVariable("subject", email.getSubject());
            blueprint.addVariable("user", userService.findByUsername(principal.getName()).getEmail());

            HtmlEmail mail = new HtmlEmail();

            if (email.getAllRecipients().size() > 0) {
                for (String recipient : email.getAllRecipients()) {
                    mail.addBcc(recipient);
                }
            }
            sent = this.mailService.send(blueprint, mail);

            //Delete temp file.
            Files.deleteIfExists(file.toPath());
        }
        
        return sent;
    }

    /**
     * Check e-mail domain.
     *
     * @param email String e-mail.
     * @return Boolean
     */
    public Boolean checkDomain(String email) {
        boolean valid = true;
        String pattern = configurationService.findByParameter("EMAIL_DOMAIN_FILTER").getValue();

        if (!pattern.isEmpty()) {
            valid = email.matches(pattern);
        }
        return valid;
    }

    /**
     * Get invalid recipients.
     *
     * @param externalRecipient String recipients.
     * @return String Invalid recipients
     */
    public String getInvalidRecipients(String externalRecipient) {
        List<String> invalidRecipient = new ArrayList<>();

        if (!externalRecipient.trim().isEmpty()) {
            for (String external : Arrays.asList(externalRecipient.split("\\,|\\;"))) {
                if (!checkDomain(external)) {
                    invalidRecipient.add(external);
                }
            }
        }
        return StringUtils.join(invalidRecipient, ",");
    }
}

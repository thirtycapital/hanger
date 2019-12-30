/*
 * Copyright (c) 2019 Dafiti Group
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
import br.com.dafiti.hanger.model.Connection;
import br.com.dafiti.hanger.model.ExportEmail;
import br.com.dafiti.hanger.model.User;
import br.com.dafiti.hanger.option.ExportType;
import br.com.dafiti.hanger.service.ConnectionService.QueryResultSet;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.UUID;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 *
 * @author Helio Leal
 */
@Service
public class ExportService {

    ConnectionService connectionService;
    MailService mailService;
    UserService userService;

    @Autowired
    public ExportService(
            ConnectionService connectionService,
            MailService mailService,
            UserService userService) {
        this.connectionService = connectionService;
        this.mailService = mailService;
        this.userService = userService;
    }

    /**
     * Run a query and export it to a format.
     *
     * @param connection
     * @param query
     * @param principal
     * @param exportType
     * @return
     */
    public String export(
            Connection connection,
            String query,
            Principal principal,
            ExportType exportType) {
        String fileName = "";

        switch (exportType) {
            case CSV:
                QueryResultSet queryResultSet = this.connectionService
                        .getQueryResultSet(connection, query, principal);

                //Identify if query ran successfully.
                if (!queryResultSet.hasError()) {
                    fileName = this.exportToCSV(queryResultSet, connection);
                }
                break;
            default:
                System.out.println("No export type selected...");
                break;
        }

        return fileName;
    }

    /**
     * Export a resultset to CSV.
     *
     * @param queryResultSet QueryResultSet
     * @param connection Connection
     * @return String fileName
     */
    public String exportToCSV(
            QueryResultSet queryResultSet,
            Connection connection) {

        String temp = System.getProperty("java.io.tmpdir");
        String fileName = connection.getName()
                .concat("_hanger_export_")
                .concat(UUID.randomUUID().toString())
                .concat(".csv");

        //Define output file.
        File csvFile = new File(temp.concat("/").concat(fileName));

        //Define csv settings.
        CsvWriterSettings csvWriterSettings = new CsvWriterSettings();
        csvWriterSettings.getFormat().setDelimiter(";");
        csvWriterSettings.getFormat().setQuote('"');
        csvWriterSettings.getFormat().setQuoteEscape('"');

        CsvWriter csvWriter = new CsvWriter(csvFile, csvWriterSettings);

        //Define fields.
        csvWriter.writeHeaders(queryResultSet.getHeader());

        queryResultSet.getRow().forEach((row) -> {
            csvWriter.writeRow(row.getColumn());
        });

        csvWriter.flush();
        csvWriter.close();

        return fileName;
    }

    /**
     * Download a file to local station.
     *
     * @param file
     * @return
     * @throws IOException
     */
    public HttpEntity<?> dowload(String file) throws IOException {
        String fileName = file.concat(".csv");

        Path path = new File(System.getProperty("java.io.tmpdir")
                .concat("/")
                .concat(fileName)).toPath();

        //Get file content as Byte.
        byte[] fileContent = Files.readAllBytes(path);

        //Prepare headers.
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(
                "Content-disposition",
                "attachment; filename=\"" + fileName + "\"");

        //Prepare file to download
        HttpEntity<byte[]> download
                = new HttpEntity<>(fileContent, httpHeaders);

        //Delete temp file.
        Files.deleteIfExists(path);

        return download;
    }

    /**
     * Export a resultset to E-mail.
     *
     * @param exportEmail
     * @param principal
     * @throws java.io.IOException
     */
    public void exportToEmail(ExportEmail exportEmail, Principal principal)
            throws IOException, Exception {

        QueryResultSet queryResultSet = this.connectionService
                .getQueryResultSet(
                        exportEmail.getConnection(),
                        exportEmail.getQuery(),
                        principal);

        //Identify if query ran successfully.
        if (!queryResultSet.hasError()) {
            String fileName = this.exportToCSV(
                    queryResultSet,
                    exportEmail.getConnection());

            File file = new File(System.getProperty("java.io.tmpdir")
                    .concat("/")
                    .concat(fileName));

            //Get temp file path.
            Path path = file.toPath();

            //Get logged user.
            User user = userService.findByUsername(principal.getName());

            //Put logged user on subject of e-mail.
            if (user != null) {
                exportEmail.setSubject(
                        exportEmail.getSubject()
                                .concat(" (")
                                .concat(user.getEmail())
                                .concat(")"));
            }

            //New blueprint.
            Blueprint blueprint = new Blueprint(exportEmail.getSubject(), "exportQuery");
            blueprint.setRecipient(exportEmail.getRecipient());
            blueprint.setFile(file);
            blueprint.addVariable("query", exportEmail.getQuery());
            blueprint.addVariable("queryContent", exportEmail.isQueryContent());
            blueprint.addVariable("connection", exportEmail.getConnection());
            blueprint.addVariable("content", exportEmail.getContent());

            //Set the HTML content to send on e-mail.
            HtmlEmail mail = new HtmlEmail();

            if (blueprint.getRecipients().size() > 0) {
                for (String recipient : blueprint.getRecipients()) {
                    mail.addTo(recipient);
                }
            }

            //Send e-mail to users.
            this.mailService.send(blueprint, mail);

            //Delete temp file.
            Files.deleteIfExists(path);
        }
    }
}

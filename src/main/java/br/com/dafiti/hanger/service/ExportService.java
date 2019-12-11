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

import br.com.dafiti.hanger.model.Connection;
import br.com.dafiti.hanger.option.ExportType;
import br.com.dafiti.hanger.service.ConnectionService.QueryResultSet;
import br.com.dafiti.hanger.service.ConnectionService.QueryResultSetRow;
import br.com.dafiti.mitt.Mitt;
import br.com.dafiti.mitt.exception.DuplicateEntityException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;
import java.util.UUID;
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

    @Autowired
    public ExportService(ConnectionService connectionService) {
        this.connectionService = connectionService;
    }

    /**
     * Run a query and export it to a format.
     *
     * @param connection
     * @param query
     * @param principal
     * @param exportType
     * @return
     * @throws br.com.dafiti.mitt.exception.DuplicateEntityException
     */
    public String export(
            Connection connection,
            String query,
            Principal principal,
            ExportType exportType) throws DuplicateEntityException {
        String fileName = "";

        switch (exportType) {
            case CSV:
                QueryResultSet queryResultSet = this.connectionService
                        .getQueryResultSet(connection, query, principal);

                //Identify if query ran successfully.
                if (!queryResultSet.hasError()) {
                    fileName = this.exportToCSV(queryResultSet);
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
     * @param queryResultSet
     * @return
     * @throws br.com.dafiti.mitt.exception.DuplicateEntityException
     */
    public String exportToCSV(QueryResultSet queryResultSet)
            throws DuplicateEntityException {

        //Define the mitt.
        Mitt mitt = new Mitt();

        String temp = System.getProperty("java.io.tmpdir");
        String fileName = UUID.randomUUID().toString().concat("_hanger_export.csv");

        //Define output file.
        mitt.setOutput(temp.concat("/").concat(fileName));

        //Define fields.
        mitt.getConfiguration().addField(queryResultSet.getHeader());

        for (QueryResultSetRow row : queryResultSet.getRow()) {
            mitt.write(row.getColumn());
        }

        mitt.close();

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
}

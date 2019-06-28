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

import br.com.dafiti.hanger.option.Phase;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Valdiney V GOMES
 */
public class JobBuildMetricFilter {

    private Phase phase;
    private String dateFrom;
    private String dateTo;

    public JobBuildMetricFilter(Date dateFrom, Date dateTo) {
        this.dateFrom = new SimpleDateFormat("yyyy-MM-dd").format(dateFrom);
        this.dateTo = new SimpleDateFormat("yyyy-MM-dd").format(dateTo);
    }

    public JobBuildMetricFilter(Phase phase, Date dateFrom, Date dateTo) {
        this.phase = phase;
        this.dateFrom = new SimpleDateFormat("yyyy-MM-dd").format(dateFrom);
        this.dateTo = new SimpleDateFormat("yyyy-MM-dd").format(dateTo);
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public String getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    public String getDateTo() {
        return dateTo;
    }

    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }
}

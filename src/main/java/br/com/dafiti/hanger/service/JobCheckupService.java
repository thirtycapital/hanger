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
package br.com.dafiti.hanger.service;

import br.com.dafiti.hanger.model.Blueprint;
import br.com.dafiti.hanger.model.Command;
import br.com.dafiti.hanger.model.CommandLog;
import br.com.dafiti.hanger.model.JobCheckup;
import br.com.dafiti.hanger.model.Job;
import br.com.dafiti.hanger.model.JobCheckupLog;
import br.com.dafiti.hanger.option.Action;
import br.com.dafiti.hanger.option.CommandType;
import br.com.dafiti.hanger.option.Flow;
import br.com.dafiti.hanger.option.Scope;
import br.com.dafiti.hanger.repository.JobCheckupRepository;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 *
 * @author Valdiney V GOMES
 */
@Service
public class JobCheckupService {

    private final JdbcTemplate jdbcTemplate;
    private final JobCheckupRepository jobCheckupRepository;
    private final ConnectionService connectionService;
    private final JenkinsService jenkinsService;
    private final JobService jobService;
    private final RetryService retryService;
    private final JobCheckupLogService jobCheckupLogService;
    private final CommandLogService commandLogService;
    private final MailService mailService;
    private final JobStatusService jobStatusService;
    private final SlackService slackService;

    @Autowired
    public JobCheckupService(
            JdbcTemplate jdbcTemplate,
            JobCheckupRepository jobCheckupRepository,
            ConnectionService connectionService,
            JenkinsService jenkinsService,
            JobService jobService,
            RetryService retryService,
            JobCheckupLogService jobCheckupLogService,
            CommandLogService commandLogService,
            MailService mailService,
            JobStatusService jobStatusService,
            SlackService slackService) {

        this.jdbcTemplate = jdbcTemplate;
        this.jobCheckupRepository = jobCheckupRepository;
        this.connectionService = connectionService;
        this.jenkinsService = jenkinsService;
        this.jobService = jobService;
        this.retryService = retryService;
        this.jobCheckupLogService = jobCheckupLogService;
        this.commandLogService = commandLogService;
        this.mailService = mailService;
        this.jobStatusService = jobStatusService;
        this.slackService = slackService;
    }

    /**
     * List all checkups.
     *
     * @return
     */
    public Iterable<JobCheckup> list() {
        return jobCheckupRepository.findAll();
    }

    /**
     * Load a checkup.
     *
     * @param id JobCheckup ID.
     * @return JobCheckup.
     */
    public JobCheckup load(Long id) {
        return jobCheckupRepository.findOne(id);
    }

    /**
     * Save a checkup.
     *
     * @param checkup JobCheckup.
     */
    public void save(JobCheckup checkup) {
        jobCheckupRepository.save(checkup);
    }

    /**
     * Delete a checkup.
     *
     * @param id JobCheckup ID.
     */
    public void delete(Long id) {
        jobCheckupRepository.delete(id);
    }

    /**
     * Identify if there are prevalidation.
     *
     * @param job Job.
     * @return Identify if a job has prevalidation.
     */
    public boolean hasPrevalidation(Job job) {
        List<JobCheckup> prevalidation = job.getCheckup()
                .stream()
                .filter(x -> (x.isPrevalidation()))
                .collect(Collectors.toList());

        return !(prevalidation.isEmpty());
    }

    /**
     * Evaluate a job.
     *
     * @param job Job.
     * @param scope Scope.
     * @return Identify if job is healthy.
     */
    public boolean evaluate(Job job, Scope scope) {
        return this.evaluate(job, false, scope);
    }

    /**
     * Evaluate a job.
     *
     * @param job Job.
     * @param prevalidation Identify if is a prevalidation.
     * @param scope Scope.
     * @return Identify if job is healthy.
     */
    public boolean evaluate(Job job, boolean prevalidation, Scope scope) {
        boolean log;
        boolean stop = false;
        boolean validated = true;

        //Identifies if the job has checkup.
        if (!job.getCheckup().isEmpty()) {
            //Log the job status before checkup evaluation.
            Logger.getLogger(JobCheckupService.class.getName()).log(Level.INFO, "{0} status before checkup evaluation", new Object[]{job.getName()});
            
            //Filter checkup by scope.
            List<JobCheckup> checkups = job.getCheckup()
                    .stream()
                    .filter(x -> (x.getScope().equals(scope) || x.getScope().equals(Scope.ANYONE)))
                    .filter(x -> (x.isPrevalidation() == prevalidation))
                    .collect(Collectors.toList());

            //Identify if the job has checkups.
            if (!checkups.isEmpty()) {
                int retry = retryService.get(job);

                for (JobCheckup checkup : checkups) {
                    //Identify the query scope and if it is enabled. 
                    if (checkup.isEnabled() && !stop) {
                        //Run the query. 
                        String value = this.executeQuery(checkup);

                        //Compare value and threshold. 
                        validated = this.check(checkup, value);

                        //Identify if is just a log. 
                        log = checkup.getAction().equals(Action.LOG_AND_CONTINUE);

                        //Identify if should retry.
                        if (retry <= job.getRetry() || (retry == 1 && job.getRetry() == 0)) {
                            //Log the checkup result. 
                            JobCheckupLog jobCheckupLog = jobCheckupLogService.save(new JobCheckupLog(checkup, value, validated));

                            //Identify if should execute something. 
                            if (!validated && !log) {
                                boolean result = false;

                                //Increase the retry counter.
                                retryService.increase(job);

                                //Execute the checkup command.
                                for (Command command : checkup.getCommand()) {
                                    result = this.executeCommand(checkup, command, jobCheckupLog);

                                    if (!result) {
                                        break;
                                    }
                                }

                                //Identify if should revalidate the checkup.
                                if (result) {
                                    //Run the query. 
                                    value = this.executeQuery(checkup);

                                    //Compare value and threshold. 
                                    validated = this.check(checkup, value);

                                    //Define the success and value to the log. 
                                    jobCheckupLog.setSuccess(validated);
                                    jobCheckupLog.setValue(value);
                                }

                                //Identify if the status changed after the command execution.
                                if (!validated) {
                                    this.executeAction(job, checkup);
                                    stop = true;
                                }
                            }

                            //Add the log to the checkup. 
                            checkup.addLog(jobCheckupLog);
                        }

                        //Verify if this check failed. 
                        if (!validated || (!validated && log)) {
                            if (job.getApprover() != null) {
                                Blueprint blueprint = new Blueprint(job.getApprover().getEmail(), "Nick Checkup failure", "checkupFailure");
                                blueprint.addVariable("approver", job.getApprover().getFirstName());
                                blueprint.addVariable("job", job.getName());
                                blueprint.addVariable("checkup", checkup.getDescription());

                                mailService.send(blueprint);
                            }

                            //Verify if this job has some notification to do.
                            if (job.isNotify()) {
                                StringBuilder message = new StringBuilder();

                                message
                                        .append(":broken_heart: ")
                                        .append("*")
                                        .append(job.getDisplayName())
                                        .append("*’s")
                                        .append(" checkup ")
                                        .append("*")
                                        .append(checkup.getDescription())
                                        .append("*")
                                        .append(" failed because the result was ")
                                        .append("*")
                                        .append(value)
                                        .append("*")
                                        .append(" but the expected is ")
                                        .append(checkup.getConditional())
                                        .append(" *")
                                        .append(checkup.getThreshold())
                                        .append("*");

                                slackService.send(message.toString(), job.getChannel());
                            }
                        }

                        //Checked will be always true when is LOG_AND_CONTINUE.
                        if (log) {
                            validated = true;
                        }
                    }
                }

                //Clear tries.
                if (validated) {
                    retryService.remove(job);
                }
            }

            //Log the job status after checkup evaluation.
            Logger.getLogger(JobCheckupService.class.getName()).log(Level.INFO, "{0} status after checkup evaluation", new Object[]{job.getName()});
        }

        return validated;
    }

    /**
     * Execute an action.
     *
     * @param job Job
     * @param checkup JobCheckup
     */
    @Async
    private void executeAction(Job job, JobCheckup checkup) {
        switch (checkup.getAction()) {
            case REBUILD:
                try {
                //Rebuild the job.
                jobStatusService.updateFlow(job.getStatus(), Flow.REBUILD);
                jenkinsService.build(job);
            } catch (URISyntaxException | IOException ex) {
                Logger.getLogger(EyeService.class.getName()).log(Level.SEVERE, "Fail building job: " + job.getName(), ex);
            }

            break;
            case REBUILD_MESH:
                HashSet<Job> parent = jobService.getMeshParent(job);
                jobService.rebuildMesh(job);

                try {
                    //Rebuild the mesh. 
                    for (Job meshParent : parent) {
                        jenkinsService.build(meshParent);
                    }
                } catch (URISyntaxException | IOException ex) {
                    Logger.getLogger(EyeService.class.getName()).log(Level.SEVERE, "Fail building job mesh: " + job.getName(), ex);
                }

                break;
            case REBUILD_TRIGGER:
                List<Job> trigger = checkup.getTrigger();

                try {
                    //Rebuild a selected job path. 
                    if (!trigger.isEmpty()) {
                        trigger.stream().map((jobFrom) -> jobService.getRelationPath(job, jobFrom)).forEach((jobPath) -> {
                            jobPath.stream().forEach((jobInPath) -> {
                                if (!job.equals(jobInPath)) {
                                    jobStatusService.updateFlow(jobInPath.getStatus(), Flow.REBUILD);
                                }
                            });
                        });

                        for (Job jobTriggered : trigger) {
                            jenkinsService.build(jobTriggered);
                        }
                    }
                } catch (URISyntaxException | IOException ex) {
                    Logger.getLogger(EyeService.class.getName()).log(Level.SEVERE, "Fail building trigger: " + job.getName(), ex);
                }

                break;
            default:
                retryService.remove(job);
                break;
        }
    }

    /**
     * Execute a checkup query.
     *
     * @param checkup JobCheckup.
     * @return Identify if the result match the threshold.
     */
    private String executeQuery(JobCheckup checkup) {
        String value = "";

        try {
            //Set a connection to database.
            jdbcTemplate.setDataSource(connectionService.getDataSource(checkup.getConnection()));
            jdbcTemplate.setMaxRows(1);

            //Execute a query. 
            value = jdbcTemplate.queryForObject(checkup.getQuery(), (ResultSet rs, int row) -> rs.getString(1));
        } catch (DataAccessException ex) {
            value = ex.getMessage();
        } finally {
            try {
                //Close the connection. 
                jdbcTemplate.getDataSource().getConnection().close();
            } catch (SQLException ex) {
                Logger.getLogger(JobCheckupService.class.getName()).log(Level.SEVERE, "Fail closing connection ", ex);
            }
        }

        return value;
    }

    /**
     * Execute a command.
     *
     * @param checkup JobCheckup.
     * @param command Command.
     * @return Identify if the command run successfully.
     */
    private boolean executeCommand(JobCheckup checkup, Command command, JobCheckupLog jobCheckupLog) {
        boolean success;

        //Identify the command type.
        if (command.getCommandType().equals(CommandType.SQL)) {
            success = this.sqlCommand(checkup, command, jobCheckupLog);
        } else {
            success = this.shellCommand(command, jobCheckupLog);
        }

        return success;
    }

    /**
     * Execute a sql command.
     *
     * @param checkup JobCheckup.
     * @param command Command.
     * @param jobCheckupLog JobCheckupLog.
     * @return Identify if the command run successfully.
     */
    private boolean sqlCommand(JobCheckup checkup, Command command, JobCheckupLog jobCheckupLog) {
        int affected;
        String log = "";
        boolean success = true;

        try {
            //Set a connection to database.
            jdbcTemplate.setDataSource(connectionService.getDataSource(checkup.getConnection()));

            //Execute a query.
            affected = jdbcTemplate.update(command.getCommand());

            //Log the affected rows.
            log = "Affected record[s]: " + affected;
        } catch (DataAccessException ex) {
            success = false;
            log = ex.getMessage();
            Logger.getLogger(JobCheckupService.class.getName()).log(Level.SEVERE, "Fail executing SQL command ", ex);
        } finally {
            try {
                //Close the connection.
                jdbcTemplate.getDataSource().getConnection().close();
            } catch (SQLException ex) {
                Logger.getLogger(JobCheckupService.class.getName()).log(Level.SEVERE, "Fail closing connection ", ex);
            }

            try {
                //Define the command log.
                CommandLog commandLog = commandLogService.save(
                        new CommandLog(
                                command,
                                log,
                                success));

                //Add the command log to checkup log.
                if (jobCheckupLog != null) {
                    jobCheckupLog.addCommandLog(commandLog);
                }
            } catch (Exception ex) {
                Logger.getLogger(JobCheckupService.class.getName()).log(Level.SEVERE, "Fail recording sql command log " + ex.getMessage(), ex);
            }
        }

        return success;
    }

    /**
     * Execute a Shell command.
     *
     * @param checkup JobCheckup.
     * @param command Command.
     * @param jobCheckupLog JobCheckupLog.
     * @return Identify if the command run successfully.
     */
    private boolean shellCommand(Command command, JobCheckupLog jobCheckupLog) {
        String log = "";
        boolean success = true;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            //Create a temp sh file.
            File tmp = File.createTempFile("hanger_", ".sh");

            //Define file permisssion.
            tmp.setExecutable(true);
            tmp.setReadable(true);
            tmp.setWritable(true);

            //Write shell command to sh file.
            try (FileWriter writer = new FileWriter(tmp)) {
                writer.write(command.getCommand().replaceAll("\r", ""));
            }

            //Parse the command to run.
            CommandLine cmdLine = CommandLine.parse("sh " + tmp);

            //Define the executor.
            DefaultExecutor executor = new DefaultExecutor();

            //Define the timeout.
            executor.setWatchdog(new ExecuteWatchdog(300000));

            //Define the work directory.
            Path sandbox = Paths.get(System.getProperty("user.home") + "/.hanger/sandbox/");
            Files.createDirectories(sandbox);
            executor.setWorkingDirectory(new File(sandbox.toString()));

            //Define the log.
            PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream, outputStream, null);
            executor.setStreamHandler(streamHandler);

            //Execute the sh file.
            success = (executor.execute(cmdLine) == 0);

            //Get the command log. 
            log = outputStream.toString();

            //Delete the sh file.
            tmp.delete();
        } catch (IOException ex) {
            success = false;
            log = ex.getMessage();
            Logger.getLogger(JobCheckupService.class.getName()).log(Level.SEVERE, "Fail executing shell command " + ex.getMessage(), ex);
        } finally {
            try {
                //Define the command log.
                CommandLog commandLog = commandLogService.save(
                        new CommandLog(
                                command,
                                log,
                                success));

                //Add the command log to checkup log.
                if (jobCheckupLog != null) {
                    jobCheckupLog.addCommandLog(commandLog);
                }
            } catch (Exception ex) {
                Logger.getLogger(JobCheckupService.class.getName()).log(Level.SEVERE, "Fail recording shell command log " + ex.getMessage(), ex);
            }
        }

        return success;
    }

    /**
     * Compare the checkup threshold with a value.
     *
     * @param checkup JobCheckup.
     * @param value Value.
     * @return Identify if passed on test.
     */
    public boolean check(JobCheckup checkup, String value) {
        boolean checked = false;
        Float finalValue = null;
        Float finalThreshold = null;
        String threshold = checkup.getThreshold();

        //Remove white spaces. 
        value = (value == null) ? "" : value.trim();
        threshold = (threshold == null) ? "" : threshold.trim();
        threshold = this.getMacro(threshold);

        //Try to convert the value to float. 
        try {
            finalValue = Float.parseFloat(value);
        } catch (NumberFormatException ex) {
            Logger.getLogger(JobCheckupService.class.getName()).log(Level.SEVERE, "Fail converting value " + value + " to float", ex);
        }

        //Try to convert the threshold to float. 
        try {
            finalThreshold = Float.parseFloat(threshold);
        } catch (NumberFormatException ex) {
            Logger.getLogger(JobCheckupService.class.getName()).log(Level.SEVERE, "Fail converting threshold " + threshold + " to float", ex);
        }

        //Check if the threshold is numeric.
        if (finalValue != null && finalThreshold != null) {
            //Check if the resultset match the threshold.
            switch (checkup.getConditional()) {
                case EQUAL:
                    checked = Objects.equals(finalValue, finalThreshold);
                    break;
                case NOT_EQUAL:
                    checked = !Objects.equals(finalValue, finalThreshold);
                    break;
                case LOWER_THAN:
                    checked = finalValue < finalThreshold;
                    break;
                case LOWER_THAN_OR_EQUAL:
                    checked = finalValue <= finalThreshold;
                    break;
                case GREATER_THAN:
                    checked = finalValue > finalThreshold;
                    break;
                case GREATER_THAN_OR_EQUAL:
                    checked = finalValue >= finalThreshold;
                    break;
                default:
                    break;
            }
        }

        return checked;
    }

    /**
     * Identify if threshold is a macro.
     *
     * @param threshold String
     * @return String macro value or threshold itself value.
     */
    private String getMacro(String threshold) {

        if (threshold.startsWith("${") && threshold.endsWith("}")) {
            //Remove all non numeric characters.
            String id = threshold.replaceAll("[^\\d.]", "");

            if (!id.isEmpty()) {
                //Load a checkup by id.
                JobCheckup checkupRelation = this.load(Long.valueOf(id));

                if (checkupRelation != null) {
                    //Find the checkup threshold value.
                    if (!checkupRelation.getLog().isEmpty()) {
                        threshold = checkupRelation.getLog().get(checkupRelation.getLog().size() - 1).getValue();
                    }
                }
            }
        }

        return threshold;
    }
}

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

import br.com.dafiti.hanger.model.SubjectDetails;
import com.slack.api.model.block.HeaderBlock;
import com.slack.api.model.block.LayoutBlock;
import com.slack.api.model.block.SectionBlock;
import com.slack.api.model.block.composition.MarkdownTextObject;
import com.slack.api.model.block.composition.PlainTextObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 *
 * @author Guilherme Oliveira
 */
@Service
public class SubjectNotificationService {

    private final SubjectService subjectService;
    private final SlackService slackService;
    private final SubjectDetailsService subjectDetailsService;
    private final ConfigurationService configurationService;

    @Autowired
    public SubjectNotificationService(
            SubjectService subjectService,
            SubjectDetailsService subjectDetailsService,
            SlackService slackService,
            ConfigurationService configurationService) {

        this.subjectService = subjectService;
        this.slackService = slackService;
        this.subjectDetailsService = subjectDetailsService;
        this.configurationService = configurationService;
    }

    /**
     * Send all subject status status on default Slack.
     */
    @Scheduled(cron = "0 0 0-23 * * ?")
    public void notifySubjectList() {
        List<SubjectDetails> subjectDetails
                = subjectDetailsService.getDetailsOf(subjectService.findAllByOrderByName());

        for (SubjectDetails detail : subjectDetails) {
            //Identifies if should notify this subject. 
            if (detail.getSubject().isNotified()) {
                String status = "";
                List<LayoutBlock> message = new ArrayList();

                //Identifies the channels to notify. 
                Set<String> channels = detail.getSubject().getChannel();

                //Identify if should notify only the default channel. 
                if (channels.isEmpty()) {
                    channels.add(
                            configurationService
                                    .findByParameter("SLACK_CHANNEL")
                                    .getValue());
                }

                //Identifies subject status.
                if (detail.getFailurePercent() > 0) {
                    status = ":red_circle:";
                } else if (detail.getSuccessPercent() == 100) {
                    status = ":large_green_circle:";
                } else if (detail.getWarningPercent() == 100) {
                    status = ":large_orange_circle:";
                }else{
                    status = ":white_circle:";
                }

                //Slack message blocks.
                message.add(
                        HeaderBlock
                                .builder()
                                .text(PlainTextObject
                                        .builder()
                                        .text(status + " " + detail.getSubject().getName())
                                        .emoji(Boolean.TRUE)
                                        .build())
                                .build());

                message.add(
                        SectionBlock
                                .builder()
                                .fields(Arrays.asList(
                                        MarkdownTextObject
                                                .builder()
                                                .text("*Waiting:* \n " + String.format("%.2f", detail.getWaitingPercent()) + "%")
                                                .build(),
                                        MarkdownTextObject
                                                .builder()
                                                .text("*Success:* \n " + String.format("%.2f", detail.getSuccessPercent()) + "%")
                                                .build(),
                                        MarkdownTextObject
                                                .builder()
                                                .text("*Failure:* \n " + String.format("%.2f", detail.getFailurePercent()) + "%")
                                                .build(),
                                        MarkdownTextObject
                                                .builder()
                                                .text("*Warning:* \n " + String.format("%.2f", detail.getWarningPercent()) + "%")
                                                .build(),
                                        MarkdownTextObject
                                                .builder()
                                                .text("*Building:* \n " + String.format("%.2f", detail.getBuildingPercent()) + "%" + " (" + (detail.getBuilding()) + " jobs)")
                                                .build()))
                                .build());

                //Send the notification. 
                slackService.send(message, channels);
            }
        }
    }
}

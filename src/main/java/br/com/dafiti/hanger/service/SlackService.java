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

import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.block.LayoutBlock;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.scheduling.annotation.Async;

/**
 * Manage Slack API.
 *
 * @author Helio Leal
 * @author Valdiney V GOMES
 *
 */
@Service
public class SlackService {

    private final Slack slack;
    private final ConfigurationService configurationService;

    private static final Logger LOG = LogManager.getLogger(SlackService.class.getName());

    @Autowired
    public SlackService(ConfigurationService configurationService) {
        this.slack = Slack.getInstance();
        this.configurationService = configurationService;
    }

    /**
     * Get the slack channel list.
     *
     * @return slack channel list.
     */
    @Cacheable(value = "slackChannels")
    public Set<String> getChannels() {
        MethodsClient client = slack.methods();
        Set<String> channels = new HashSet();

        try {
            ConversationsListResponse result = client
                    .conversationsList(
                            r -> r.token(
                                    configurationService
                                            .findByParameter("SLACK_BOT_TOKEN")
                                            .getValue()));

            for (Conversation channel : result.getChannels()) {
                if (channel.getName() != null) {
                    channels.add(channel.getName());
                }
            }
        } catch (IOException
                | SlackApiException ex) {
            LOG.log(Level.ERROR, "Fail getting slack channels", ex);
        }

        return channels;
    }

    /**
     * Posts a text nessage in a default Slack channel.
     *
     * @param message Slack message.
     */
    public void send(String message) {
        this.send(message, new HashSet<>());
    }

    /**
     * Posts a blocks nessage in a default Slack channel.
     *
     * @param message Slack message.
     */
    public void send(List<LayoutBlock> message) {
        this.send(message, new HashSet<>());
    }

    /**
     * Posts a blocks message in a Slack channel.
     *
     * @param message Slack message.
     */
    void send(List<LayoutBlock> message, Set<String> channels) {
        this.send(message, channels, true);
    }

    /**
     * Posts a text message in a Slack channel.
     *
     * @param message Slack message.
     * @param channels Slack channels.
     */
    public void send(String message, Set<String> channels) {
        this.send(message, channels, false);
    }

    /**
     * Posts a message in a Slack channel.
     *
     * @param message Slack message.
     * @param channels Slack channels.
     * @param blocks Identifies if it is a blocks message.
     */
    @Async
    public void send(Object message, Set<String> channels, boolean blocks) {
        MethodsClient client = slack.methods();

        if (channels.isEmpty()) {
            channels.add(
                    configurationService
                            .findByParameter("SLACK_CHANNEL")
                            .getValue());
        }

        for (String channel : channels) {
            try {
                if (blocks) {
                    client.chatPostMessage(r -> r
                            .token(
                                    configurationService
                                            .findByParameter("SLACK_BOT_TOKEN")
                                            .getValue())
                            .channel(channel)
                            .blocks((List<LayoutBlock>) message)
                    );
                } else {
                    client.chatPostMessage(r -> r
                            .token(
                                    configurationService
                                            .findByParameter("SLACK_BOT_TOKEN")
                                            .getValue())
                            .channel(channel)
                            .text((String) message)
                    );
                }

                LOG.log(Level.INFO, "Slack message posted to " + channel);
            } catch (IOException | SlackApiException ex) {
                LOG.log(Level.ERROR, "Fail posting message to channel" + channel, ex);
            }
        }
    }

    /**
     *
     */
    @Caching(evict = {
        @CacheEvict(value = "slackChannels", allEntries = true)})
    public void refresh() {
    }
}

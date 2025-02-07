/*
 *  This file is part of OpenTSDB.
 *  Copyright (C) 2021 Yahoo.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.opentsdb.horizon.alerting.corona.processor.emitter.slack.formatter;

import net.opentsdb.horizon.alerting.corona.model.alert.AlertType;
import net.opentsdb.horizon.alerting.corona.model.alert.Comparator;
import net.opentsdb.horizon.alerting.corona.model.alert.State;
import net.opentsdb.horizon.alerting.corona.model.alert.WindowSampler;
import net.opentsdb.horizon.alerting.corona.model.alert.impl.SingleMetricSimpleAlert;
import net.opentsdb.horizon.alerting.corona.model.alertgroup.AlertGroup;
import net.opentsdb.horizon.alerting.corona.model.alertgroup.GroupKey;
import net.opentsdb.horizon.alerting.corona.model.contact.Contact;
import net.opentsdb.horizon.alerting.corona.model.contact.impl.SlackContact;
import net.opentsdb.horizon.alerting.corona.model.messagekit.MessageKit;
import net.opentsdb.horizon.alerting.corona.model.messagekit.meta.SlackMeta;
import net.opentsdb.horizon.alerting.corona.processor.emitter.slack.client.api.SlackBuilders;
import net.opentsdb.horizon.alerting.corona.processor.emitter.slack.client.api.SlackColor;
import net.opentsdb.horizon.alerting.corona.processor.emitter.slack.client.api.SlackRequest;
import net.opentsdb.horizon.alerting.corona.processor.emitter.slack.client.impl.SlackBuildersImpl;
import net.opentsdb.horizon.alerting.corona.processor.emitter.view.Views;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlackSingleMetricAlertFormatterTest {

    final SlackBuilders slackBuilders = SlackBuildersImpl.instance();

    final SlackSingleMetricAlertFormatter formatter =
            new SlackSingleMetricAlertFormatter(slackBuilders);

    @Test
    void testFormat()
    {
        final long alertId = 2020L;
        final long alertTsSec = 1581118871L;

        final MessageKit mk = MessageKit.builder()
                .setType(Contact.Type.SLACK)
                .setMeta(SlackMeta.builder()
                        .setSubject("Subject: Slack Alert")
                        .setBody("Body: Slack Alert")
                        .setLabels("label1", "label2")
                        .build()
                )
                .setContacts(SlackContact.builder()
                        .setName("Slack Contact Name")
                        .setEndpoint("http://slack.edpoint.url")
                        .build()
                )
                .setAlertGroup(AlertGroup.builder()
                        .setGroupKey(GroupKey.builder()
                                .setAlertId(alertId)
                                .setAlertType(AlertType.SINGLE_METRIC)
                                .setNamespace("OpenTSDB")
                                .setKeys("host")
                                .setValues("localhost")
                                .build()
                        )
                        .setAlerts(
                                SingleMetricSimpleAlert.builder()
                                        .setId(alertId)
                                        .setNamespace("OpenTSDB")
                                        .setMetric("cpu.utilization.pct")
                                        .addTag("host", "localhost")
                                        .addTag("env", "prod")
                                        .setTimestampSec(alertTsSec)
                                        .setStateFrom(State.GOOD)
                                        .setState(State.BAD)
                                        .setComparator(Comparator.GREATER_THAN)
                                        .setThreshold(80)
                                        .setWindowSizeSec(180)
                                        .setSampler(WindowSampler.AT_LEAST_ONCE)
                                        .setValuesInWindow(9, 89, 59)
                                        .setTimestampsSec(
                                                alertTsSec - 2 * 60,
                                                alertTsSec - 60,
                                                alertTsSec
                                        )
                                        .setIsSnoozed(true)
                                        .setIsNag(false)
                                        .setDetails("Very detailed info")
                                        .build()
                        )
                        .build()
                )
                .build();

        final SlackRequest request = slackBuilders.createRequestBuilder()
                .setText("*[OpenTSDB] Subject: Slack Alert*\n" +
                        "_*cpu.utilization.pct &gt; 80.000000 at least once* in the last *3 minutes*_\n" +
                        "*1* Good -&gt; *Bad*, showing 1. Grouped by [host:*localhost*]."
                )
                .setAttachments(Arrays.asList(
                        slackBuilders.createAttachmentBuilder()
                                .setMarkdownIn("text")
                                .setColor(SlackColor.DANGER)
                                .setText("Value: *59.0*  _Tags:_ env:*prod*  host:*localhost*")
                                .build(),
                        slackBuilders.createAttachmentBuilder()
                                .setColor(SlackColor.NEUTRAL)
                                .setMarkdownIn("text")
                                .setText("<" + Views.alertSplunkUrl(alertId,
                                        alertTsSec * 1000) + "|View details> | " +
                                        "<" + Views.alertEditUrl(alertId) + "|Modify alert>\n" +
                                        "Body: Slack Alert"
                                )
                                .build()
                ))
                .build();
        assertEquals(request, formatter.format(mk).get(0));
    }
}

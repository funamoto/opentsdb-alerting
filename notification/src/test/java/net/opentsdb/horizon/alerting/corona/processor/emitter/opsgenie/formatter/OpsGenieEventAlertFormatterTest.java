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

package net.opentsdb.horizon.alerting.corona.processor.emitter.opsgenie.formatter;

import net.opentsdb.horizon.alerting.corona.TestData;
import net.opentsdb.horizon.alerting.corona.model.alert.AlertType;
import net.opentsdb.horizon.alerting.corona.model.contact.Contact;
import net.opentsdb.horizon.alerting.corona.model.messagekit.MessageKit;
import net.opentsdb.horizon.alerting.corona.processor.emitter.opsgenie.OpsGenieAlert;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class OpsGenieEventAlertFormatterTest {

    @Test
    public void eventAlert()
    {
        EventAlertOpsGenieFormatter ogFormatter =
                new EventAlertOpsGenieFormatter(
                        "OpenTSDB-Dev-Team",
                        "OpenTSDB"
                );

        MessageKit messageKit = TestData.getMessageKit(
                Contact.Type.OPSGENIE,
                AlertType.EVENT
        );

        OpsGenieAlert ogAlert = ogFormatter.format(messageKit);

        String expectedNote = "<span><strong>2 Bad.</strong> <em>Number of events in Yahoooo-Good filtered by `*:*` has been less than 10 in the last 10 minutes.</em></span>\n" +
                "</br>\n" +
                "<span>Event count: <strong>5</strong>. <em>Tags: </em>host:<strong>remotehost</strong>, owner:<strong>chiruvol</strong></span>\n" +
                "</br>\n" +
                "<span>Event count: <strong>5</strong>. <em>Tags: </em>host:<strong>remotehost</strong>, owner:<strong>chiruvol</strong></span>\n" +
                "<span><em>Last event: </em>\n" +
                "    [docker] <strong>Important matter</strong></span>\n" +
                "<span>Blah, blah, blah</span>\n" +
                "<span><em>Tags: </em>deployment: <strong>good</strong></span>\n" +
                "<hr width=\"33%\" align=\"left\"/>";

        assertEquals("a1a905ef03a4fc3fbbfaea0f51d803cafc68552301b6dcba0cf7e88f73e41f3c",
                ogAlert.getAlias());
        assertEquals("Single Metric Alert. Host: localhost<br/><br/><strong>Grouped by</strong>:<br/>host:<strong>localhost</strong>".trim(),
                ogAlert.getDescription().trim());
        assertEquals(2,
                ogAlert.getDetails().size());
        assertEquals("https://splunk.opentsdb.net/splunk/en-US/app/search/search?q=search+index%3Dcorona-alerts+alert_id%3D0+earliest%3D07%2F23%2F2019%3A18%3A50%3A00UTC+latest%3D07%2F23%2F2019%3A19%3A05%3A00UTC+timeformat%3D%25m%2F%25d%2F%25Y%3A%25H%3A%25M%3A%25S%25Z",
                ogAlert.getDetails().get("OpenTSDB View Details"));
        assertEquals("https://opentsdb.net/a/0/view",
                ogAlert.getDetails().get("OpenTSDB Modify Alert"));
        assertEquals("CPU High Alert. Host: localhost",
                ogAlert.getMessage());
        assertEquals("OpenTSDB",
                ogAlert.getNamespace());
        assertEquals(StringUtils.deleteWhitespace(expectedNote),
                TestData.deleteCopyright(StringUtils.deleteWhitespace(ogAlert.getNote())));
        assertEquals("P5",
                ogAlert.getPriority());
        assertEquals(Collections.singletonList("Test_moog_1"),
                ogAlert.getVisibleToTeams());
        assertEquals("OpenTSDB",
                ogAlert.getSource());
        assertArrayEquals(new String[] {"host-localhost"},
                ogAlert.getTags());
        assertEquals("OpenTSDB-Dev-Team",
                ogAlert.getUser());
    }
}

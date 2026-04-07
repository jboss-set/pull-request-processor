package org.jboss.set.pull.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.jboss.set.pull.processor.data.ReportItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action which produces report if proper flag has been set.
 *
 * @author baranowb
 * @author wangc
 *
 */
public class PullProcessorReporting {

    private static final Logger LOG = LoggerFactory.getLogger(PullProcessorReporting.class.getName());
    /**
     * @param reportFile
     * @param reportItems
     */
    public void writeReport(List<ReportItem> reportItems, File reportFile) {
        LOG.info("Start writing report to file: {} a total of {} records", reportFile, reportItems.size());
        try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(reportFile)))) {
            writer.write("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "table, th, td {\n" +
                "  border: 1px solid black;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<table>\n" +
                "  <tr>\n" +
                "    <th>Pull Request</th>\n" +
                "    <th>Issue</th>\n" +
                "    <th>Current Labels</th>\n" +
                "    <th>Add Labels</th>\n" +
                "    <th>Remove Labels</th>\n" +
                "  </tr>");

            for(int i=0 ;i<reportItems.size();i++) {
                ReportItem ri = reportItems.get(i);
                writer.write("<tr>\n" +
                   "    <td>" + ri.getUrl() + "</td>\n" +
                   "    <td>" +ri.getIssue() + "</td>\n" +
                   "    <td>" +ri.getCurrentLabels() + "</td>\n" +
                   "    <td>" +ri.getAddLabels() + "</td>\n" +
                   "    <td>" +ri.getRemoveLabels() + "</td>\n" +
                   "  </tr>");
            }

            writer.write("</table>\n" +
               "\n" +
               "</body>\n" +
               "</html>");
            LOG.info("Finish writing report to file: {}", reportFile);
        } catch (IOException e) {
            LOG.error("Error during action reporting", e);
        }

    }

}

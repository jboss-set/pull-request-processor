package org.jboss.set.pull.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.jboss.set.pull.processor.data.EvaluatorReportEntry;
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
        try (PrintWriter w = new PrintWriter(new BufferedWriter(new FileWriter(reportFile)))) {
            w.write("<!DOCTYPE html>\n<html>\n<head>\n<style>\n");
            w.write("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; margin: 20px; }\n");
            w.write(".pr-section { margin-bottom: 24px; border: 1px solid #ddd; border-radius: 6px; padding: 16px; }\n");
            w.write(".pr-section h3 { margin-top: 0; }\n");
            w.write(".pr-section h3 a { color: #0366d6; text-decoration: none; }\n");
            w.write(".labels { margin-bottom: 12px; }\n");
            w.write(".lbl { display: inline-block; padding: 2px 8px; margin: 2px; border-radius: 12px; font-size: 12px; }\n");
            w.write(".lbl-current { background: #e1e4e8; color: #24292e; }\n");
            w.write(".lbl-add { background: #dcffe4; color: #22863a; }\n");
            w.write(".lbl-remove { background: #ffdce0; color: #cb2431; }\n");
            w.write("table { border-collapse: collapse; width: 100%; font-size: 13px; }\n");
            w.write("th, td { border: 1px solid #ddd; padding: 6px 10px; text-align: left; }\n");
            w.write("th { background: #f6f8fa; }\n");
            w.write("tr:nth-child(even) { background: #fafbfc; }\n");
            w.write(".eval-name { vertical-align: top; font-weight: 600; background: #f6f8fa; }\n");
            w.write(".type-read { color: #6a737d; }\n");
            w.write(".type-computed { color: #0366d6; font-weight: 600; }\n");
            w.write("</style>\n</head>\n<body>\n");
            w.write("<h1>Pull Request Processor Report</h1>\n");

            for (ReportItem ri : reportItems) {
                w.write("<div class=\"pr-section\">\n");
                w.write("<h3><a href=\"" + ri.getUrl() + "\">" + prRef(ri.getUrl()) + "</a>");
                if (!"n/a".equals(ri.getIssue())) {
                    w.write(" &mdash; <a href=\"" + ri.getIssue() + "\">" + issueRef(ri.getIssue()) + "</a>");
                }
                w.write("</h3>\n");

                w.write("<div class=\"labels\">\n");
                for (String label : ri.getCurrentLabels()) {
                    w.write("<span class=\"lbl lbl-current\">" + label + "</span>\n");
                }
                for (String label : ri.getAddLabels()) {
                    w.write("<span class=\"lbl lbl-add\">+ " + label + "</span>\n");
                }
                for (String label : ri.getRemoveLabels()) {
                    w.write("<span class=\"lbl lbl-remove\">- " + label + "</span>\n");
                }
                w.write("</div>\n");

                List<EvaluatorReportEntry> entries = ri.getEvaluatorEntries();
                if (entries != null && !entries.isEmpty()) {
                    w.write("<table>\n<tr><th>Evaluator</th><th>Field</th><th>Type</th><th>Value</th></tr>\n");
                    for (EvaluatorReportEntry entry : entries) {
                        List<EvaluatorReportEntry.ReportField> fields = entry.getFields();
                        for (int j = 0; j < fields.size(); j++) {
                            EvaluatorReportEntry.ReportField field = fields.get(j);
                            w.write("<tr>");
                            if (j == 0) {
                                w.write("<td class=\"eval-name\" rowspan=\"" + fields.size() + "\">" + entry.getEvaluator() + "</td>");
                            }
                            w.write("<td>" + field.getName() + "</td>");
                            w.write("<td class=\"type-" + field.getType() + "\">" + field.getType() + "</td>");
                            w.write("<td>" + field.getValue() + "</td></tr>\n");
                        }
                    }
                    w.write("</table>\n");
                }
                w.write("</div>\n");
            }

            w.write("</body>\n</html>");
            LOG.info("Finish writing report to file: {}", reportFile);
        } catch (IOException e) {
            LOG.error("Error during action reporting", e);
        }

    }

    private static String prRef(String url) {
        String[] parts = url.split("/");
        if (parts.length >= 5) {
            return parts[parts.length - 3] + "#" + parts[parts.length - 1];
        }
        return url;
    }

    private static String issueRef(String url) {
        int idx = url.lastIndexOf('/');
        return idx >= 0 ? url.substring(idx + 1) : url;
    }

}

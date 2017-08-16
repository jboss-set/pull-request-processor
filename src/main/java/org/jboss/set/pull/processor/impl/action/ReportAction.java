package org.jboss.set.pull.processor.impl.action;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.set.pull.processor.Action;
import org.jboss.set.pull.processor.ActionContext;
import org.jboss.set.pull.processor.data.EvaluatorData;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class ReportAction implements Action {

    private static final Logger logger = Logger.getLogger("org.jboss.set.pull.processor");

    @Override
    public void execute(ActionContext actionContext, List<EvaluatorData> data) {
        try {
            File file = generateReport(data, actionContext.getFileName());
            logger.info("report generated: " + file);
        } catch (IOException | TemplateException | URISyntaxException e) {
            logger.log(Level.SEVERE, "something happened during report generation", e);
        }

    }
    private File generateReport(List<EvaluatorData> data, String fileName) throws IOException, TemplateException, URISyntaxException {
        Configuration cfg = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
        cfg.setClassForTemplateLoading(this.getClass(), "/META-INF/");
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        /* Create a data-model */
        Map<String, Object> input = new HashMap<>();
        input.put("rows", data);

        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate("report.ftl");

        /* Merge data-model with template */
        File file = new File(fileName);
        Writer out = new FileWriter(file);
        temp.process(input, out);
        return file;
    }

}

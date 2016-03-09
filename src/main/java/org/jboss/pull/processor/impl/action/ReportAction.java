package org.jboss.pull.processor.impl.action;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.pull.processor.Action;
import org.jboss.pull.processor.ActionContext;
import org.jboss.pull.processor.data.ProcessorData;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class ReportAction implements Action {

	private static final Logger logger = Logger.getLogger("org.jboss.pull.processor");
	
	@Override
	public void execute(ActionContext actionContext, List<ProcessorData> data) {
		try {
			String fileName = "/home/egonzalez/pull-processor/report.html";
			File file = generateReport(data, fileName);
    		logger.info("report generated: " + file);
		} catch (IOException | TemplateException | URISyntaxException e) {
			logger.log(Level.SEVERE, "something happened during report generation", e);
		}

	}
    private File generateReport(List<ProcessorData> data, String fileName) throws IOException, TemplateException, URISyntaxException {
        Configuration cfg = new Configuration();
        URI url = this.getClass().getClassLoader().getResource("META-INF/").toURI();
        cfg.setDirectoryForTemplateLoading(new File(url));
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

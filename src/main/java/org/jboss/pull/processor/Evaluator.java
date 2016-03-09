package org.jboss.pull.processor;

import java.util.Map;


public interface Evaluator {

	String name();
	
    void eval(EvaluatorContext context, Map<String, Object> data);
    
}

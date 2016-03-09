package org.jboss.pull.processor.data;

import java.util.HashMap;
import java.util.Map;


public class ProcessorData {
		
	private Map<String, Object> data;
	
	public ProcessorData(Map<String, Object> data) {
		this.data = data;
	}
	
	public ProcessorData() {
		this.data = new HashMap<>();
	}
	
	public Map<String, Object> getData() {
		return data;
	}
	
}
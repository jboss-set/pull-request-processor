package org.jboss.pull.processor.impl.evaluator;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.set.aphrodite.domain.FlagStatus;
import org.jboss.set.aphrodite.domain.Issue;

public final class Util {
	private Util() {}
	
	private static final Pattern patternStreamFlagBuzilla = Pattern.compile("[0-9]\\.[0-9]\\.[0-9z]");
	private static final Pattern patternStreamFlagJira = Pattern.compile("[0-9]\\.[a-zA-Z]*(\\.[0-9z])?(\\.[a-zA-Z]*)?");
	
	public static List<String> getStreams(Issue issue) {
		
		EnumSet<FlagStatus> set = EnumSet.of(FlagStatus.ACCEPTED, FlagStatus.SET);
		List<String> streams = new ArrayList<>();
		Map<String, FlagStatus> statuses = issue.getStreamStatus();
		for(Map.Entry<String, FlagStatus> status : statuses.entrySet()) {
			String stream = extract(status.getKey());
			if(stream != null && set.contains(status.getValue())) {
				streams.add(stream);
			}
		}
		return streams;
	}
	
	public static String extract(String value) {
		Matcher matcherBugzilla = patternStreamFlagBuzilla.matcher(value);
		Matcher matcherJira = patternStreamFlagJira.matcher(value);
		if (matcherBugzilla.find()) {
		    String bugzilla =  matcherBugzilla.group();
		    return (bugzilla.length() >= 5 ? bugzilla.substring(bugzilla.length() - 5) : bugzilla);
		} else if(matcherJira.find()){
		    return matcherJira.group();
		} else {
		    return null;
		}
	}
}

package org.jboss.set.pull.processor.impl.evaluator.util;

import java.net.URI;

public final class LogUtil {

    private static final int EVALUATOR_WIDTH = 20;

    private LogUtil() {}

    public static String prRef(URI prURI) {
        String path = prURI.getPath();
        String[] parts = path.split("/");
        if (parts.length >= 5) {
            return parts[2] + "#" + parts[4];
        }
        return prURI.toString();
    }

    public static String pad(String evaluatorName) {
        return String.format("%-" + EVALUATOR_WIDTH + "s", evaluatorName);
    }
}

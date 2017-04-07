package org.jboss.pull.processor;

import org.jboss.pull.processor.processes.eap7.ProcessorEAP7;
import org.jboss.pull.processor.processes.eap7.ProcessorMerge;

public class Main {

    public static void main(String[] argv) throws Exception {

        // Run merge processing
        if (Boolean.getBoolean("merge")) {
            if (argv.length == 2) {
                try {
                    ProcessorMerge processor = new ProcessorMerge(argv[0], argv[1]);
                    processor.run();
                    System.exit(0);
                } catch (Exception e) {
                    System.err.println(e);
                    e.printStackTrace(System.err);
                }
            } else {
                System.err.println(usageMerge());
            }
        }

        // Run milestone processing
        if (Boolean.getBoolean("processEAP7")) {
            try {
                ProcessorEAP7 processor = new ProcessorEAP7();
                processor.run();
                System.exit(0);
            } catch (Exception e) {
                System.err.println(e);
                e.printStackTrace(System.err);
            }
        }

        System.err.println(usage());
        System.exit(1);
    }

    private static String usage() {
        StringBuilder usage = new StringBuilder();
        usage.append("Enable processing via any combination of:\n");
        usage.append("-Dmerge\n");
        usage.append("-Dmilestone\n");
        return usage.toString();
    }

    private static String usageMerge() {
        StringBuilder usage = new StringBuilder();
        usage.append("java -jar pull-processor-1.0-SNAPSHOT.jar <property name of the target branch on github> <property name of dedicated jenkins merge job>\n\n");
        usage.append(common());
        return usage.toString();
    }

    private static StringBuilder common() {
        StringBuilder usage = new StringBuilder();
        usage.append("optional system properties:\n");
        usage.append("-Dprocessor.properties.file defaults to \"./processor.properties\"\n");
        usage.append("-Ddryrun=true to run without changing anything, i.e. simulated run, defaults to false\n");
        return usage;
    }
}

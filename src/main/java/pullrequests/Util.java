/**
 * Internal Use Only
 *
 * Copyright 2011 Red Hat, Inc. All rights reserved.
 */
package pullrequests;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

class Util {

    static Properties loadProperties() throws IOException {
        final String propsFilePath = System.getProperty("processor.properties.file", "./processor.properties");

        final Properties props = new Properties();
        props.load(new FileReader(new File(propsFilePath)));
        return props;
    }

    static String require(Properties props, String name) {
        final String ret = (String) props.get(name);
        if (ret == null)
            throw new RuntimeException(name + " must be specified in processor.properties");

        return ret;
    }

    static String get(Properties props, String name) {
        return (String) props.get(name);

    }

}

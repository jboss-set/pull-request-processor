package org.jboss.pull.processor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class Bugzilla {

    public static final Pattern BUGZILLAIDPATTERN = Pattern.compile("bugzilla\\.redhat\\.com/show_bug\\.cgi\\?id=(\\d+)",
            Pattern.CASE_INSENSITIVE);

    private static final String BUGZILLA_BASE = "bugzilla.base=https://bugzilla.redhat.com/";
    private static String BUGZILLA_LOGIN;
    private static String BUGZILLA_PASSWORD;

    static {
        Properties props;
        try {
            props = Util.loadProperties();
            BUGZILLA_LOGIN = Util.require(props, "bugzilla.login");
            BUGZILLA_PASSWORD = Util.require(props, "bugzilla.password");
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }

    /**
     * Get a new XmlRpcClient instance from server URL
     *
     * @param serverURL Bugzilla base URL
     * @return XmlRpcClient
     */
    private static XmlRpcClient getClient(String serverURL) {
        try {
            String apiURL = serverURL + "xmlrpc.cgi";
            XmlRpcClient rpcClient;
            XmlRpcClientConfigImpl config;
            config = new XmlRpcClientConfigImpl();
            config.setServerURL(new URL(apiURL));
            rpcClient = new XmlRpcClient();
            rpcClient.setConfig(config);
            return rpcClient;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Post a new comment on Bugzilla
     *
     * @param bugzillaId Bugzilla identity
     * @param comment The comment will be posted
     * @return true if post successes
     */
    public static boolean postBugzillaComment(String bugzillaId, String comment) {
        System.out.println(comment);
        Integer id = Integer.valueOf(bugzillaId);
        try {
            XmlRpcClient rpcClient = getClient(BUGZILLA_BASE);
            Map<Object, Object> params = new HashMap<Object, Object>();

            params.put("Bugzilla_login", BUGZILLA_LOGIN);
            params.put("Bugzilla_password", BUGZILLA_PASSWORD);
            params.put("id", id);
            params.put("comment", comment);
            Object[] objs = { params };
            Object result = (HashMap) rpcClient.execute("Bug.add_comment", objs);

            return true;

        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Change Bugzilla bug status to Bug.Status status.
     *
     * @param bugzillaId Bugzilla identity
     * @param status Bug status
     * @return true if status changed
     */
    @SuppressWarnings("unchecked")
    public static boolean updateBugzillaStatus(String bugzillaId, Bug.Status status) {
        Integer id = Integer.valueOf(bugzillaId);
        try {
            XmlRpcClient rpcClient = getClient(BUGZILLA_BASE);
            Map<Object, Object> params = new HashMap<Object, Object>();

            // update bug status.
            params.put("Bugzilla_login", BUGZILLA_LOGIN);
            params.put("Bugzilla_password", BUGZILLA_PASSWORD);
            params.put("ids", id);
            params.put("status", status);
            Object[] objs1 = { params };
            Object result = (HashMap) rpcClient.execute("Bug.update", objs1);

            // check bug status now should be MODIFIED.
            params.remove("status");
            Object[] objs2 = { params };
            result = (HashMap) rpcClient.execute("Bug.get", objs2);
            Map<Object, Object> res = (Map<Object, Object>) result;
            Object[] bugs = (Object[]) res.get("bugs");
            if (bugs.length >= 0) {
                Object bugObject = bugs[0];
                HashMap<String, Object> bugMap = (HashMap<String, Object>) bugObject;
                Bug bug = new Bug(bugMap);
                return bug.getStatus().equals(status);
            } else
                return false;

        } catch (XmlRpcException e) {
            e.printStackTrace();
        }
        return false;
    }
}
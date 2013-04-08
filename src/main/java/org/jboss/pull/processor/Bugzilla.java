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
import org.jboss.pull.processor.Flag.Status;

public class Bugzilla {

    public static final Pattern BUGZILLAIDPATTERN = Pattern.compile("bugzilla\\.redhat\\.com/show_bug\\.cgi\\?id=(\\d+)",
            Pattern.CASE_INSENSITIVE);

    private static final String BUGZILLA_BASE = "https://bugzilla.redhat.com/";
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
    public static boolean postBugzillaComment(Integer bugzillaId, String comment) {
        System.out.println(comment);
        try {
            XmlRpcClient rpcClient = getClient(BUGZILLA_BASE);
            Map<Object, Object> params = new HashMap<Object, Object>();

            params.put("Bugzilla_login", BUGZILLA_LOGIN);
            params.put("Bugzilla_password", BUGZILLA_PASSWORD);
            params.put("id", bugzillaId);
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
     * Change Bugzilla bug status.
     *
     * @param bugzillaId id. The ids of the bugs that you want to modify
     * @param status The status you want to change the bug to
     * @return true if status changed otherwise false
     */
    @SuppressWarnings("unchecked")
    public static boolean updateBugzillaStatus(Integer bugzillaId, Bug.Status status) {
        try {
            XmlRpcClient rpcClient = getClient(BUGZILLA_BASE);
            Map<Object, Object> params = new HashMap<Object, Object>();

            // update bug status.
            params.put("Bugzilla_login", BUGZILLA_LOGIN);
            params.put("Bugzilla_password", BUGZILLA_PASSWORD);
            params.put("ids", bugzillaId);
            params.put("status", status);
            Object[] objParams = { params };
            Object result = (HashMap) rpcClient.execute("Bug.update", objParams);
            Map<Object,Object> res = (Map<Object, Object>) result;
            int id = (Integer) res.get("id");
            return id == bugzillaId;

        } catch (XmlRpcException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Update Bugzilla bugs flag status.
     *
     * @param ids An array of integers, a single integer representing one or more bug ids
     * @param name The name of the flag that supposes to be updated
     * @param status The flag's new status(+,-,X,?)
     * @return true if update successful, otherwise false;
     */
    @SuppressWarnings("unused")
    private static boolean updateBugzillaFlag(Integer[] ids, String name, Status status) {

        String flagStatus;
        if(status.equals(Status.POSITIVE))
            flagStatus = "+";
        else if(status.equals(Status.NEGATIVE))
            flagStatus = "-";
        else if (status.equals(Status.UNKNOWN))
            flagStatus = "?";
        else
            flagStatus = " ";

        XmlRpcClient rpcClient = getClient(BUGZILLA_BASE);
        Map<Object, Object> params = new HashMap<Object, Object>();

        HashMap<String, String> updates = new HashMap<String, String>();
        updates.put("name", name);
        updates.put("status", flagStatus);
        Object[] updateArray = {updates};

        //update bugzilla bugs flag.
        params.put("Bugzilla_login", BUGZILLA_LOGIN);
        params.put("Bugzilla_password", BUGZILLA_PASSWORD);
        params.put("ids", ids);
        params.put("updates", updateArray);
        params.put("permissive", true);

        Object[] objs = {params};
        try {
            rpcClient.execute("Flag.update", objs);
            return true;
        } catch (XmlRpcException e) {
            e.printStackTrace();
            return false;
        }
    }
}
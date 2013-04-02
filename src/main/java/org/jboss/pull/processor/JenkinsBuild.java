/*
 * JBoss, Home of Professional Open Source.
 * Copyright (c) 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.pull.processor;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import org.jboss.dmr.ModelNode;

/**
 * @author <a href="mailto:istudens@redhat.com">Ivo Studensky</a>
 * @author Jason T. Greene
 */
public class JenkinsBuild {
    private String status;
    private int build;

    private JenkinsBuild(int build, String status) {
        this.build = build;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public int getBuild() {
        return build;
    }

    private static JenkinsBuild findBuild(int build, String base, String job,  Map<String, String> params) {
       InputStream stream = null;
        try {
            URL url = new URL(base + "/job/" + job + "/" + build + "/api/json");
            URLConnection urlConnection = url.openConnection();
            stream = urlConnection.getInputStream();
            ModelNode node = ModelNode.fromJSONStream(stream);

            if (params == null) {
                String status = node.hasDefined("result") ? node.get("result").asString() : null;
                return new JenkinsBuild(build, status);
            }

            ModelNode parameters = node.get("actions").get(0).get("parameters");
            int matches = 0;
            for (ModelNode parameter : parameters.asList()) {
                String value = params.get(parameter.get("name").asString());
                String jenkinsValue = parameter.get("value").asString();
                if (value != null && jenkinsValue != null && jenkinsValue.indexOf(value) != -1) {   //FIXME replace indexOf with a regexp matching with \W surrounding the value
                    if (++matches >= params.size()) {
                        String status = node.hasDefined("result") ? node.get("result").asString() : null;
                        return new JenkinsBuild(build, status);
                    }
                }
            }
        } catch (Exception e) {
            System.err.printf("Could not process build: %d on job: %s: %s", build, job, e.getMessage());
        } finally {
            Util.safeClose(stream);
        }

        return null;
    }

     public static JenkinsBuild findBuild(String base, String job, Map<String, String> params) {
         InputStream stream = null;
         try {
             URL url = new URL(base + "/job/" + job + "/api/json");
             URLConnection urlConnection = url.openConnection();
             stream = urlConnection.getInputStream();

             ModelNode node = ModelNode.fromJSONStream(stream);
             ModelNode builds = node.get("builds");
             for (ModelNode buildNode : builds.asList()) {
                 int buildNum = buildNode.get("number").asInt();
                 JenkinsBuild build  = findBuild(buildNum, base, job, params);
                 if (build != null)
                     return build;
             }
        } catch (Exception e) {
            throw new IllegalStateException("Could not obtain build list", e);
        } finally {
            Util.safeClose(stream);
        }

        return null;
    }

    public static JenkinsBuild findLastBuild(String base, String job) {
        InputStream stream = null;
        try {
            URL url = new URL(base + "/job/" + job + "/api/json");
            URLConnection urlConnection = url.openConnection();
            stream = urlConnection.getInputStream();

            ModelNode node = ModelNode.fromJSONStream(stream);
            ModelNode builds = node.get("builds");
            int lastBuildNum = 0;
            for (ModelNode buildNode : builds.asList()) {
                int buildNum = buildNode.get("number").asInt();
                if (buildNum > lastBuildNum)
                    lastBuildNum = buildNum;
            }
            if (lastBuildNum > 0) {
                JenkinsBuild build = findBuild(lastBuildNum, base, job, null);
                if (build != null)
                    return build;
            }
       } catch (Exception e) {
           throw new IllegalStateException("Could not obtain build list", e);
       } finally {
           Util.safeClose(stream);
       }

       return null;
   }

    public static boolean isPending(String base, String job, Map<String, String> params) {
        InputStream stream = null;
        try {
            URL url = new URL(base + "/queue/api/json");
            stream = url.openStream();
            ModelNode node = ModelNode.fromJSONStream(stream);
            ModelNode builds = node.get("items");
            for (ModelNode buildNode : builds.asList()) {
                if (job.equals(buildNode.get("task", "name").asString())) {
                    ModelNode parameters = buildNode.get("actions").get(0).get("parameters");
                    int matches = 0;
                    for (ModelNode parameter : parameters.asList()) {
                        String value = params.get(parameter.get("name").asString());
                        if (value != null && value.equals(parameter.get("value").asString())) {
                            if (++matches >= params.size()) {
                                return true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not obtain pending list", e);
        } finally {
            Util.safeClose(stream);
        }

        return false;
    }

}

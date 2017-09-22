[![Build Status](https://travis-ci.org/jboss-set/pull-request-processor.svg?branch=master)](https://travis-ci.org/jboss-set/pull-request-processor)
pull-request-processor
======================

A processor which checks open PRs, verifies whether they are mergeable and triggers a given Hudson job in order to merge them.
It also checks the status of the latest merge on Hudson, post comments on github, etc.

Way to invoke

- -s streams to be processed, this should list streams and components that should be scrutinized. It must contain comma separated list of stream and components belonging to stream: streamName[comp1,comp2],stream2Name[comp3,comp4]. Check jboss streams for ids.

- -p : defines streams and components that are eligible to be written into. This is selective write permission. It has the same structure as list in -s. If it is not present, it defaults to -s

- -w: true or false - determine if write pemission has been granted

- -r: root path where processor can write output if it supports it

Example: 
java -jar -Daphrodite.config=${PULL_REQUEST_PROCESSOR_HOME}/aphrodite-test.json -Dstreams.json=${PULL_REQUEST_PROCESSOR_HOME}/streams.json ${PULL_REQUEST_PROCESSOR_HOME}/target/pull-processor-0.8.0-SNAPSHOT.jar -s jboss-eap-7.0.z[jbossas-jboss-eap7,jbossas-wildfly-core-eap] -p jboss-eap-7.0.z[jbossas-jboss-eap7,jbossas-wildfly-core-eap] -r ${PULL_REQUEST_PROCESSOR_HOME}/ -w false

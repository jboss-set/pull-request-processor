[![Build Status](https://travis-ci.org/jboss-set/pull-request-processor.svg?branch=master)](https://travis-ci.org/jboss-set/pull-request-processor)
pull-request-processor
======================

A processor which checks open PRs, verifies whether they are mergeable and triggers a given Hudson job in order to merge them.
It also checks the status of the latest merge on Hudson, post comments on github, etc.

Way to invoke

- -s streams to be processed

- -as : allowed streams to be label

- -d: true or false (execution in dryRun mode for write operation in Github)

- -r: where to place the html report

java -jar -Daphrodite.config=${PULL_REQUEST_PROCESSOR_HOME}/aphrodite.json -Dstreams.json=${PULL_REQUEST_PROCESSOR_HOME}/streams.json /${PULL_REQUEST_PROCESSOR_HOME}/pull-processor.jar -s ${STREAMS} -r ${WORKSPACE}/report.html -d true  -as 7.0.z.GA

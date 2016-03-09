pull-request-processor
======================

A processor which checks open PRs, verifies whether they are mergeable and triggers a given Hudson job in order to merge them.
It also checks the status of the latest merge on Hudson, post comments on github, etc.

Way to invoke

java -jar pull-request-processor-<version>.jar -Daphrodite.config=$PULLPROCESSOR_HOME/aphrodite.json -Dstreams.json=$PULLPROCESSOR_HOME/streams.json <stream name>


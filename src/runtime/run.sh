cd ../../build/libs
mkdir -p ../logs
CONFIG_DIR=../../src/runtime
java \
-Darchaius.configurationSource.additionalUrls=file:$CONFIG_DIR/archaius.properties \
-Xloggc:../logs/gcstats.log \
-Dspring.config.location=file:$CONFIG_DIR/application.yml \
-Darchaius.fixedDelayPollingScheduler.delayMills=10000 \
-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps  \
-XX:MaxInlineSize=1024 -XX:MaxInlineLevel=20 -XX:InlineSmallCode=10000 -XX:FreqInlineSize=10000 \
-jar patternmatcher-1.0-SNAPSHOT.jar

#!/bin/bash
cd "$(dirname "$0")"

source ../../conf/exportEnvVar.sh taxonomy-global taxonomy-ws;
ps aux | grep taxonomy | grep ws | awk "{print \$2}" |  xargs kill;
#jar -uvf taxonomy-ws-0.0.1-SNAPSHOT.war WEB-INF
$javaBinary -jar -Dspring.profiles.active=${profile} $@ -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=9003 ${wsPackageFolder}/taxonomy-ws-0.0.1-SNAPSHOT.war 

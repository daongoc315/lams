#!/bin/sh
java -classpath lib/lams-tool-deploy.jar:lib/commons-configuration-1.1.jar:lib/commons-lang-2.6.jar:lib/commons-collections-3.2.2.jar:lib/commons-logging-1.2.jar:lib/commons-io-2.5.jar:lib/commons-dbutils-1.7.jar:lib/mysql-connector-java-8.0.11.jar:lib/xstream-1.4.10.jar org.lamsfoundation.lams.tool.deploy.Deploy ./deploy.xml

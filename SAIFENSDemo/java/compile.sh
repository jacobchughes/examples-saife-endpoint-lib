#!/bin/bash

export JARS="jars"
set -x
javac -d bin -cp ${JARS}/guava-11.0.jar:${JARS}/java-lib-2.1.0-int-shrlock-dev-25.jar:${JARS}/simple-xml-2.3.2.jar:${JARS}/gson-2.2.2.jar:${JARS}/aws-java-sdk-1.10.26.jar:${JARS}/aws-java-sdk-1.10.26-sources.jar:${JARS}/aws-java-sdk-flow-build-tools-1.10.26.jar src/com/saife/sample/S3Sample.java 

set +x
#aws-java-sdk-1.10.26.jar	  aws-java-sdk-1.10.26-sources.jar	     base64-2.3.8.jar  guava-11.0.jar			      java-lib-2.1.0-int-shrlock-dev-25-javadoc.jar  simple-xml-2.3.2.jar
#aws-java-sdk-1.10.26-javadoc.jar  aws-java-sdk-flow-build-tools-1.10.26.jar  gson-2.2.2.jar    java-lib-2.1.0-int-shrlock-dev-25.jar  jsr305-1.3.9.jar


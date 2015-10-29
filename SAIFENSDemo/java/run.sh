#!/bin/bash

export JARS="jars"
export LIBS="lib"
set -x
java -cp bin:${JARS}/guava-11.0.jar:${JARS}/java-lib-2.1.0-int-shrlock-dev-25.jar:${JARS}/simple-xml-2.3.2.jar:${JARS}/gson-2.2.2.jar:${JARS}/aws-java-sdk-1.10.26.jar:${JARS}/aws-java-sdk-1.10.26-sources.jar:${JARS}/aws-java-sdk-flow-build-tools-1.10.26.jar  -Djava.library.path=${LIBS} com.saife.sample.S3Sample

set +x
#base64-2.3.8.jar  guava-11.0.jar  java-lib-2.1.0-int-shrlock-dev-25.jar  java-lib-2.1.0-int-shrlock-dev-25-javadoc.jar	jsr305-1.3.9.jar  simple-xml-2.3.2.jar

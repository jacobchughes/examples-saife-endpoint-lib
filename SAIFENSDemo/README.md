# C++
The following instructions are relative to the cpp directory.

## Build

### Install the SAIFE libs and headers from their zip file. Check the include and lib directories for the libraries and header files.
    unzip <latest libsaife zip file>

### Create the output directory.
    mkdir bin

### Create the storage directory.  The name must match the code.
    mkdir black_data

## Compile and set up library path

### For Linux:
    ~$ SAIFE_HOME=\<path to cpp lib\>; g++ -std=c++11 -I$SAIFE_HOME/include -L$SAIFE_HOME/lib/saife -o bin/SAIFENS src/SAIFENS.cpp -l saife -l CecCryptoEngine -l roxml
    ~$ export LD_LIBRARY_PATH=\<path to cpp lib\>/lib/saife:${LD_LIBRARY_PATH}

### For Darwin:
    ~$ SAIFE_HOME=\<path to cpp lib\>; clang++ -std=c++11 -stdlib=libc++ -I$SAIFE_HOME/include -L$SAIFE_HOME/lib/saife -o bin/SAIFENS src/SAIFENS.cpp -l saife -l CecCryptoEngine -l roxml
    ~$ export DYLD_LIBRARY_PATH=\<path to cpp lib\>/lib/saife:${DYLD_LIBRARY_PATH}

## To Run

### 1. Create the keystore.
    ./bin/SAIFENS

### 2. Provision ./.SaifeStore/newkey.smcsr at the SAIFE dashboard.

### 3. Encrypt and store a file in the black_data directory.
    ./bin/SAIFENS -i <a_file_to_store> -s

### 4. Retrieve an encrypted file.
    ./bin/SAIFENS -i <a_file_to_retrieve> -o <the_new_file_name>




# JAVA
## Dependencies
	gson-2.3.1.jar 
	base64-2.3.8.jar
	quava-11.0.jar
	java-lib-2.1.0.jar 
	simple-xml-2.3.2.jar
### the following are for AWS/S3
	spring-core-3.0.7.jar
	spring-beans-3.0.7.jar
	spring-context-3.0.7.jar
	javax.mail-api-1.4.6.jar
	freemarker-2.3.18.jar
	aspectjrt.jar
	aspectjweaver.jar
	commons-codec-1.6.jar
	jackson-annotations-2.5.3.jar
	commons-logging-1.1.3.jar
	httpclient-4.3.6.jar
	httpcore-4.3.3.jar
	joda-time-2.8.1.jar
	jackson-databind-2.5.3.jar
	jackson-core-2.5.3.jar
	aws-java-sdk-flow-build-tools-1.10.26.jar
	aws-java-sdk-1.10.26-javadoc.jar
	aws-java-sdk-1.10.26-sources.jar
	aws-java-sdk-1.10.26.jar


[Contact SAIFE to obtain SAIFE Endpoint Library.](http://saifeinc.com/company/contact_us/)

[SAIFE Management Dashboard](https://dashboard.saifeinc.com/) account with <i/>at least one</i> organization and group

[AWS libraries are available from Amazon](https://aws.amazon.com/sdk-for-java/)

All commands are relative to the java or java_swing directory.  For ease of use,
these commands can be put into scripts.  See documentation for your specific OS
for more information.

## Amazon Web Services(AWS) Credentials
The libraries used for accessing AWS expect credentials in the
`~/.aws/credentials` file.  For more information on AWS credentials, please view 
[Amazon's Documentation](http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html).

## Implementations
This repository comes with 2 versions of the Saife Network Share demo: a
Graphical User Interface(GUI) implementation, and a Command Line Interface(CLI)
implementation.  The GUI version is found in the `java_swing/` directory, and
the CLI version is found in the `java/` directory.  Compilation is the same for
either version, but running is slightly different.

## Build
    # create the bin directory, if it does not exist
    ~$ mkdir bin/
    # add needed libraries to java class path
    ~$ export SAIFE_HOME=\<path to java lib\>
    ~$ export MY_CP="$SAIFE_HOME/jars/gson-2.3.1.jar:$SAIFE_HOME/jars/base64-2.3.8.jar: ... :$SAIFE_HOME/jars/aws-java-sdk-1.10.26.jar"
    # compile
    ~$ javac -d bin -cp $MY_CP src/com/saife/sample/*.java

## Run
If you have run this program before, but want to start from a clean slate, make
sure you delete the .SaifeStore directory, and that the AWS S3 bucket you are 
using has no Network Share Keys(NSK). If this is the first time, then run the 
program using the following command:

    ~$ java -cp bin:$MY_CP -Djava.library.path=$SAIFE_HOME/lib com.saife.demo.S3Sample.java

If you are running the GUI client, you need to select a Network Share to prompt
the creation of a new CSR. After running once, you will need to provision the newkey.smcsr at the Saife 
Dashboard.  Run again to exercise the S3 library.

    ~$ java -cp bin:$MY_CP -Djava.library.path=$SAIFE_HOME/lib com.saife.demo.S3Sample.java

# Point-To-Point Secure Messaging
This example shows using the [SAIFE library](https://www.saifeinc.com/) to send
secure messages to another SAIFE endpoint. 

## Java

#### Dependencies 
- [Google Gson](https://github.com/google/gson)
- [SAIFE SDK](http://saifeinc.com/developers/)
- [Google Guava](https://github.com/google/guava) (included in SAIFE SDK)
- [iHarder Base64](http://mvnrepository.com/artifact/net.iharder/base64) 
    (included in SAIFE SDK)
- [Simple XML](http://simple.sourceforge.net/) (included in SAIFE SDK)

#### Quick Use Guide

1. Run the program once to generate a certificate signing request
2. Provision the CSR via the [SAIFE Dashboard](https://dashboard.saifeinc.com)
3. Re-launch the program 

### Compiling

All the following commands are run from the `java/` directory.

    # set the path to the libraries
    SAIFE_HOME=./lib

    # make the bin directory, if needed
    mkdir bin/

    # set the class path
    CP="$SAIFE_HOME/base64-2.3.8.jar:\
    $SAIFE_HOME/gson.jar:\
    $SAIFE_HOME/guava-18.0.jar:\
    SAIFE_HOME/simple-xml-2.3.2.jar:\
    $SAIFE_HOME/java-lib-2.1.0-1-int-groupmessage-dev-18.jar

    # compile 
    javac -cp $CP src/com/saife/sample/*.java -d bin/

### Running

The first time you run the program, it will create a Certificate Singing
Request. 

    # set the path to the libraries
    SAIFE_HOME=./lib

    # set the class path
    CP="$SAIFE_HOME/base64-2.3.8.jar:\
    $SAIFE_HOME/gson.jar:\
    $SAIFE_HOME/guava-18.0.jar:\
    SAIFE_HOME/simple-xml-2.3.2.jar:\
    $SAIFE_HOME/java-lib-2.1.0-1-int-groupmessage-dev-18.jar

    # run
    java -cp $CP:bin/ -Djava.library.path=$SAIFE_HOME/lib/ com.saife.sample.GroupMsgSample

After you have provisioned the CSR on the [SAIFE
Dashboard](https://dashboard.saifeinc.com), run the program again to use. In
order to take full advantage of the Secure Group Messaging feature, make sure
the certificate you are provisioning is in at least one Omnigroup.

To create a new Secure Messaging Group, click the `new` button, select an
Omnigroup, select the group members(hold `ctrl` or `command` for multiple
contacts), give it a name, and click create.

If you are the group owner, you can edit or delete a Group using the
corresponding buttons.

To send and receive messages from a Group, highlight the group in the left
list(hit the `refresh` button if there are none, or create one) and click
`select`.  Then, type your message in the bottom right text field and send via
the button or enter. Messages from the selected group are automatically accepted
and displayed.


## C++

1. Create an initial keystore if one is not already present and adds the key to
the SAIFE network using the SAIFE Dashboard.
2. Sends a secure message containing "Hello World" to the first contact in the 
Secure Contact List.
3. Displays any secure messages received from its' contacts.

### Building

#### Dependencies
* GCC
* GNU Make
* SAIFE SDK

#### Compiling
```shell
SAIFE_SDK_HOME=<path to SAIFE SDK> make
```

### Running
Create two directories once for Alice and one for Bob and run the app in
separate consoles from each directory as follows.

```shell
LD_LIBRARY_PATH=$(SAIFE_SDK_HOME)/lib/saife <path to demo app>/saife_msg_demo
```

To send message to other device both devices need to be placed in the same 
contact group manually from the SAIFE Management Console after the initial 
provisioning using the credentials provided by SAIFE.



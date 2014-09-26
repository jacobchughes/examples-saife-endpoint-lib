# Point-To-Point Secure Messaging
This example shows using the [SAIFE library](https://www.saifeinc.com/) to send secure messages to another SAIFE endpoint. 


1. Create an initial keystore if one is not already present and adds the key to the SAIFE network using the SAIFE Dashboard 
2. Sends a secure message containing "Hello World" to the first contact in the Secure Contact List.  
3. Displays any secure messages received from its' contacts.

## Building

### Dependencies
* GCC
* GNU Make
* SAIFE SDK

### Compiling
```shell
SAIFE_SDK_HOME=<path to SAIFE SDK> make
```

## Running
Create two directories once for Alice and one for Bob and run the app in separate consoles from each directory as follows.

```shell
LD_LIBRARY_PATH=$(SAIFE_SDK_HOME)/lib/saife <path to demo app>/saife_msg_demo
```

To send message to other device both devices need to be placed in the same contact group manually from the SAIFE Management Console after the initial provisioning using the credentials provided by SAIFE.



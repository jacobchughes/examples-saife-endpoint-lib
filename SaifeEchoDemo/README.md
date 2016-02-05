# Dependencies
[gson](https://github.com/google/gson) - gson-2.3.1.jar or newer

[SAIFE Endpoint Library](http://saifeinc.com/developers/libraries/) - version 2.1.0 or newer.  Contact SAIFE to obtain SAIFE Endpoint Library.

[SAIFE Management Dashboard](https://dashboard.saifeinc.com/) account with <i/>at least one</i> organization and group

# Java
All commands are relative to the java directory.

### Build
<code>
~$ SAIFE_HOME=\<path to java lib\>; javac -d bin -cp $SAIFE_HOME/java-lib-2.0.1.jar:$SAIFE_HOME/simple-xml-2.3.2.jar:gson-2.3.1.jar src/com/saife/demo/SaifeEcho.java
</code>

### Run
To run as an echo server, do not provide any arguments:

<code>
~$ SAIFE_HOME=\<path to java lib\>; java -cp bin:$SAIFE_HOME/java-lib-2.0.1.jar:$SAIFE_HOME/guava-11.0.jar:gson-2.3.1.jar  -Djava.library.path=$SAIFE_HOME/lib com.saife.demo.SaifeEcho
</code>

To run as a messenger, give the following arguments:

    -c<servername> -msg <list of messages>

Where <servername> is the name of the provisioned cert and <list of messages> is
a series of strings to send to the server.

<code>
~$ SAIFE_HOME=\<path to java lib\>; java -cp bin:$SAIFE_HOME/java-lib-2.0.1.jar:$SAIFE_HOME/guava-11.0.jar:gson-2.3.1.jar  -Djava.library.path=$SAIFE_HOME/lib com.saife.demo.SaifeEcho -cServer1 -msg one two three
</code>

# C++
All commands are relative to the cpp directory.

### Build
For linux: 
<code>
~$ SAIFE_HOME=\<path to cpp lib\>; g++ -std=c++11 -I$SAIFE_HOME/include -L$SAIFE_HOME/lib/saife -o SaifeEcho src/SaifeEcho.cpp -l saife -l CecCryptoEngine -l roxml
</code>

For darwin (MAC OS X):
<code>
~$ SAIFE_HOME=\<path to cpp lib\>; clang++ -std=c++11 -stdlib=libc++ -I$SAIFE_HOME/include -L$SAIFE_HOME/lib/saife -o SaifeEcho src/SaifeEcho.cpp -l saife -l CecCryptoEngine -l roxml
</code>

### Run

For Linux: 
<code>
~$ LD_LIBRARY_PATH=\<path to cpp lib\>/lib/saife ./SaifeEcho
</code>

For darwin (MAC OS X):
<code>
~$ DYLD_LIBRARY_PATH=\<path to cpp lib\>/lib/saife ./SaifeEcho
</code>

# Running
Edit the command lines below for appropriate paths.   The application will create the SAIFE keystore and exit after the first run. Contact SAIFE to connect your application instances to the SAIFE network.


1. Create directories for instances of the app. For example <p/>
<code>
~$ mkdir EchoServer <p/>
~$ mkdir EchoCli1 <p/>
~$ mkdir EchoCli2
</code>
2. Run the server in the Echo Server Directory.  <p/>
<code>
~$ cd EchoServer <p/>
~$ LD_LIBRARY_PATH=<path to cpp lib>/lib/saife .../SaifeEcho
</code>
Expect to see three ouput logs on the initial start up containing details like:<p/>
<code>
[info]    SecurityInitializer: SecurityInitializer caught Not Keyed exception <p/>
[info]    ContactListManagerImpl: Error while loading contact list: Algorithm provider is not available.</p>
[info]    MessagingStore: CecException while load persisted decrypted messages. Algorithm provider is not available.
</code>
3. View the contents of the CSR located in the hidden directory<p/>
<code>
cat EchoServer/.SaifeStore/newkey.smcsr
</code>
4. Create a certificate using the CSR and CAPS on the [SAIFE Management Dashboard](https://dashboard.saifeinc.com/) and add it to contact group
5. Repeat Steps 2-4 to provision all instances of the application: EchoCli1 and EchoCli2 in their respective directories
6. Run a client that uses secure messaging.<p/>
<code>
~$ cd EchoCli1 <p/>
~$ LD_LIBRARY_PATH=<path to cpp lib>/lib/saife .../SaifeEcho -cEchoServer -msg one two three four five
</code>
7. Run a client that uses secure sessions.<p/>
<code>
~$ cd EchoCli2 <p/>
~$ LD_LIBRARY_PATH=<path to cpp lib>/lib/saife .../SaifeEcho -cEchoServer -sess six seven eight nine ten
</code>

# Dependencies
[gson](https://github.com/google/gson) - gson-2.2.2.jar or newer

[SAIFE Endpoint Library](http://saifeinc.com/developers/libraries/) - version 2.0.1 or newer.  Contact SAIFE to obtain SAIFE Endpoint Library.

[SAIFE Management Dashboard](https://dashboard.saifeinc.com/) account with <i/>at least one</i> organization and group

# Java
All commands are relative to the java directory.

### Build
<code>
~$ SAIFE_HOME=\<path to java lib\>; javac -d bin -cp $SAIFE_HOME/java-lib-2.0.1.jar:$SAIFE_HOME/simple-xml-2.3.2.jar:gson-2.3.1.jar src/com/saife/demo/saifeVol.java
</code>

### Run
<code>
~$ SAIFE_HOME=\<path to java lib\>; java -cp bin:$SAIFE_HOME/java-lib-2.0.1.jar:$SAIFE_HOME/guava-11.0.jar:gson-2.3.1.jar  -Djava.library.path=$SAIFE_HOME/lib com.saife.demo.saifeVol
</code>


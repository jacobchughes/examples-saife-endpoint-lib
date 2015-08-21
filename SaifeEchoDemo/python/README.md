# Dependencies

# Things not done yet. 

threading for server side
exceptions handling 
session server data parsing

[SAIFE Endpoint Library](http://saifeinc.com/developers/libraries/) - version 2.0.1 or newer.  Contact SAIFE to obtain SAIFE Endpoint Library.

[SAIFE Management Dashboard](https://dashboard.saifeinc.com/) account with <i/>at least one</i> organization and group

# Python
All commands are relative to the pyhton directory.

### Run Server 
<code>
~$ PYTHONPATH=path to python lib> LD_LIBRARY_PATH=path to python lib>/saife python SaifeEcho.py
</code>

### Run Message Client 
<code>
~$ PYTHONPATH=path to python lib> LD_LIBRARY_PATH=path to python lib>/saife python SaifeEcho.py -c EchoServer -msg one two three four
</code>

### Run Sesion Client 
<code>
~$ PYTHONPATH=path to python lib> LD_LIBRARY_PATH=path to python lib>/saife python SaifeEcho.py -c EchoServer -sess one two three four
</code>



# C++
All commands are relative to the cpp directory.

### Build

# untar the saife libs to lib/saife in the cpp dir.
# untar the saife heeaders to cpp/include

<code>
~$ SAIFE_HOME=\<path to cpp lib\>; g++ -std=c++11 -I$SAIFE_HOME/include -L$SAIFE_HOME/lib/saife -o bin/SAIFENS src/SAIFENS.cpp -l saife -l CecCryptoEngine -l roxml
</code>

### Run
<code>
~$ export LD_LIBRARY_PATH=\<path to cpp lib\>/lib/saife

# Create the storage directory.  The name must match the code.
mkdir black_data
mkdir include
mkdir bin

#create the keystore
./bin/SAIFENS  

# provision ./.SaifeStore/newkey.smcsr at dashboard

#store a file
./bin/SAIFENS -i <a_file_to_store> -s

#retrieve an encrypted file
./bin/SAIFENS -i <a_file_to_retrieve> -o <the_new_file_name>

</code>

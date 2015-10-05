# C++
The following instructions are relative to the cpp directory.

### Build

# Install the SAIFE libs and headers from their zip file. Check the include and lib directories for the libraries and header files.
<code>
unzip <latest libsaife zip file>
</code>

# Create the output directory.
<code>
mkdir bin
</code>

# Create the storage directory.  The name must match the code.
<code>
mkdir black_data
</code>

# Compile and set up library path

# For Linux:
<code>
~$ SAIFE_HOME=\<path to cpp lib\>; g++ -std=c++11 -I$SAIFE_HOME/include -L$SAIFE_HOME/lib/saife -o bin/SAIFENS src/SAIFENS.cpp -l saife -l CecCryptoEngine -l roxml
~$ export LD_LIBRARY_PATH=\<path to cpp lib\>/lib/saife:${LD_LIBRARY_PATH}
</code>

# For Darwin:
<code>
~$ SAIFE_HOME=\<path to cpp lib\>; clang++ -std=c++11 -stdlib=libc++ -I$SAIFE_HOME/include -L$SAIFE_HOME/lib/saife -o bin/SAIFENS src/SAIFENS.cpp -l saife -l CecCryptoEngine -l roxml
~$ export DYLD_LIBRARY_PATH=\<path to cpp lib\>/lib/saife:${DYLD_LIBRARY_PATH}
</code>

### To Run

# 1. Create the keystore.
<code>
./bin/SAIFENS  
</code>

# 2. Provision ./.SaifeStore/newkey.smcsr at the SAIFE dashboard.

# 3. Encrypt and store a file in the black_data directory.
<code>
./bin/SAIFENS -i <a_file_to_store> -s
</code>

# 4. Retrieve an encrypted file.
<code>
./bin/SAIFENS -i <a_file_to_retrieve> -o <the_new_file_name>
</code>

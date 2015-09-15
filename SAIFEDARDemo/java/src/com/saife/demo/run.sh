#!/bin/bash
set -x 
if [[ $# -eq 0 ]]; then
	echo "use -f <stored_file_name> -o <output_file_name> to retrieve a file."
	echo "use -s -f <input_file_name> to store a file. Input file must be in current directory."
	echo "use -r to remove and recreate volume."
	exit 0
fi

export depends="lib/java-lib-2.1.0-dev-63.jar lib/gson-2.2.2.jar lib/guava-11.0.jar lib/SAIFE/libsaife.so lib/SAIFE/libCecCryptoEngine.so lib/SAIFE/libroxml.so"
for lib in $depends; do
	if [[ ! -e $lib ]]; then
		echo "ERROR: missing $lib"
		exit 0
	fi
done

java -cp bin:lib/java-lib-2.1.0-dev-63.jar:lib/gson-2.2.2.jar:lib/guava-11.0.jar -Djava.library.path=./lib/SAIFE com.saife.demo.SaifeVol ${*}

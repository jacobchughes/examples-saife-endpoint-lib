#java -cp bin:java-lib-2.1.0-dev-63.jar:gson-2.2.2.jar:guava-11.0.jar -Djava.library.path=./lib/ com.saife.demo.saifeVol -fsample.txt -s
if [[ $# -eq 0 ]]; then
	echo "use -f<file_name> to retrieve a file."
	echo "use -s -f<file_name> to store a file."
	exit 0
fi
echo "java -cp bin:lib/java-lib-2.1.0-dev-63.jar:lib/gson-2.2.2.jar:lib/guava-11.0.jar -Djava.library.path=./lib/SAIFE com.saife.demo.saifeVol ${*}"
java -cp bin:lib/java-lib-2.1.0-dev-63.jar:lib/gson-2.2.2.jar:lib/guava-11.0.jar -Djava.library.path=./lib/SAIFE com.saife.demo.saifeVol ${*}

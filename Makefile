compile:
	javac -d out -classpath .:out/lib/* src/*.java

db:
	cd db; \
	mysql -p < db_structure.sql 

run-server:
	echo "<< You need to run this with sudo >>"
	sudo java -Xmx2G -classpath out src.Server
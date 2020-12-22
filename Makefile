compile:
	javac -d out -classpath .:out/lib/* src/*.java

db:
	cd db; \
	mysql -p < db_structure.sql 

run-server:
	echo "<< You need to run this with sudo >>"
	cd out; \
	sudo java -classpath .:lib/* src.Server
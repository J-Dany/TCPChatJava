compile:
	javac -d out -classpath .:./mysql-connector-java-8.0.22.jar src/*.java

db:
	cd db; \
	mysql -p < db_structure.sql 

run-server:
	echo "<< You need to run this with sudo >>"
	cd out; \
	sudo java -classpath .:./mysql-connector-java-8.0.22.jar src.Server
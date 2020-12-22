compile:
	javac -d out -classpath .:lib/json-20201115.jar src/*.java

db:
	cd db; \
	mysql -p < db_structure.sql 

run-server:
	echo "<< You need to run this with sudo >>"
	cd out; \
	sudo java -classpath .:lib/json-20201115.jar src.Server
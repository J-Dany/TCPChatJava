compile:
	javac -d out src/*.java

db:
	cd db; \
	mysql -p < db_structure.sql 

run-server:
	echo "<< You need to run this with sudo >>"
	cd out; \
	sudo java src.Server
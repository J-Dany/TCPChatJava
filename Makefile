compile:
	javac -d out src/*.java

run-server:
	echo "<< You need to run this with sudo >>"
	cd out; \
	sudo java src.Server
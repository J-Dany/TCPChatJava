compile:
	./gradlew build

install:
	mkdir -p /var/chat
	cp ./Server/build/libs/Server.jar /var/chat
	cp ./Client/build/libs/Client.jar /var/chat
	echo "#!/bin/bash\n" \
	"java -jar /var/chat/Server.jar" > /bin/chat-server
	echo "#!/bin/bash\n" \
	"java -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -jar /var/chat/Client.jar" > /bin/chat-client
	chmod +x /bin/chat-client
	chmod +x /bin/chat-server
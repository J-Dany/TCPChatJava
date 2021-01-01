compile:
	./gradlew build

install:
	# Metto i file jar nella cartella /lib
	mkdir -p /usr/lib/tcpchat
	cp -vr ./out/lib/* /usr/lib/tcpchat
	# Installo il server
	jar cfm ChatServer.jar out/META-INF/MANIFEST-SERVER.MF
	mv ChatServer.jar /bin/
	touch /bin/chat-server
	echo "#!/bin/bash\njava -jar /bin/ChatServer.jar" > /bin/chat-server
	chmod +x /bin/chat-server
	mkdir -p /bin/src
	cp -rv ./out/src /bin
	# Installo il client
	jar cfm AppClient.jar out/META-INF/MANIFEST-CLIENT.MF
	mv AppClient.jar /bin/
	touch /bin/chat-client
	echo "#!/bin/bash\njava -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -jar /bin/AppClient.jar" > /bin/chat-client
	chmod +x /bin/chat-client
	mkdir -p /bin/srcclient
	cp -rv ./out/srcclient /bin

#!/bin/bash
cd out
java -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -classpath .:lib/* srcclient.AppClient $1 $2
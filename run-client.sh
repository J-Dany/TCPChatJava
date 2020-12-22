#!/bin/bash
cd out
java -Dawt.useSystemAAFontSettings=on -Dswing.aatext=true -classpath .:./lib/json-20201115.jar src.AppClient $1 $2
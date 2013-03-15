#!/bin/bash

java -Djava.library.path=Server/lwjgl-natives -jar Server/BombermanServer.jar &
echo $! > pids
sleep 3

for ARG in $*; do
	if [[ "$ARG" == *jar ]]; then
		java -jar "$ARG" &
		echo $! >> pids
	else
		python "$ARG" &
		echo $! >> pids
	fi
	sleep 1
done

sleep 1
echo "#######################"
echo -n "Press enter to quit : "
read

kill $(<"pids")
rm pids

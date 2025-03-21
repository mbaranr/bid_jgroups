#!/bin/bash

echo "Select server type:"
echo "1) Frontend"
echo "2) Backend"
read -p "Enter your choice (1 or 2): " choice

if [ "$choice" == "1" ]; then
    SERVER="FrontendServer"
elif [ "$choice" == "2" ]; then
    SERVER="BackendServer"
else
    echo "Invalid choice. Exiting..."
    exit 1
fi

mkdir -p bin
javac -cp jgroups-3.6.20.jar -d bin $(find . -name "*.java")
java -cp "bin:jgroups-3.6.20.jar:." -Djava.net.preferIPv4Stack=true -Djgroups.bind_addr=127.0.0.1 server.$SERVER
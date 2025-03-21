mkdir -p bin
javac -cp jgroups-3.6.20.jar -d bin $(find . -name "*.java")
java -cp bin:jgroups-3.6.20.jar client.Client
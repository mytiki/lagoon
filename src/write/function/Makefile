.PHONY: clean

compile:
	cd ../layer && make install
	mvn compile

build: compile
	mvn package
	mkdir -p target/lambda/
	cp -r target/write-function-*.jar target/lambda/write-function.jar

clean:
	mvn clean
	rm -rf target

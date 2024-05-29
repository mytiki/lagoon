.PHONY: clean

install:
	mvn install

build: install
	mkdir -p target/lambda/java/lib
	cp -r target/write-layer-*.jar target/lambda/java/lib
	cd target/lambda && zip -r layer-content.zip java

clean:
	mvn clean
	rm -rf target

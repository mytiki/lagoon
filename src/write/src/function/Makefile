.PHONY: clean

DIST = ../../../../dist
MODULE = write
PACKAGE = function
VERSION = $(shell cat ../../../../version.txt)

compile:
	xmlstarlet ed -L -u "_:project/_:version" -v "$(VERSION)" pom.xml
	xmlstarlet ed -L -u "/_:project/_:dependencies/_:dependency[_:groupId='com.mytiki.lagoon' and _:artifactId='write-layer-aws']/_:version" -v "$(VERSION)" pom.xml
	xmlstarlet ed -L -u "/_:project/_:dependencies/_:dependency[_:groupId='com.mytiki.lagoon' and _:artifactId='write-layer-iceberg']/_:version" -v "$(VERSION)" pom.xml
	cd ../layer-aws && make install
	cd ../layer-iceberg && make install
	mvn compile

build: compile
	mvn package
	mkdir -p target/lambda/
	mkdir -p $(DIST)/assets/deploy/$(MODULE)/$(VERSION)
	cp target/write-function-*.jar $(DIST)/assets/deploy/$(MODULE)/$(VERSION)/$(PACKAGE).jar

clean:
	mvn clean
	rm -rf target .aws-sam $(DIST)/assets/deploy/$(MODULE)/$(VERSION)/$(PACKAGE).jar

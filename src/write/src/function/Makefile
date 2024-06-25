.PHONY: clean semver

DIST = ../../../../dist
MODULE = write
PACKAGE = function

semver:
	xmlstarlet ed -L -u "_:project/_:version" -v "$(version)" pom.xml
	xmlstarlet ed -L -u "/_:project/_:dependencies/_:dependency[_:groupId='com.mytiki.lagoon' and _:artifactId='write-layer-aws']/_:version" -v "$(version)" pom.xml
	xmlstarlet ed -L -u "/_:project/_:dependencies/_:dependency[_:groupId='com.mytiki.lagoon' and _:artifactId='write-layer-iceberg']/_:version" -v "$(version)" pom.xml

compile:
	cd ../layer-aws && make install
	cd ../layer-iceberg && make install
	mvn compile

build: compile
	mvn package
	mkdir -p target/lambda/
	mkdir -p $(DIST)/assets/deploy/$(MODULE)
	cp target/write-function-*.jar $(DIST)/assets/deploy/$(MODULE)/$(PACKAGE).jar

clean:
	mvn clean
	rm -rf target .aws-sam $(DIST)/assets/deploy/$(MODULE)/$(PACKAGE).jar

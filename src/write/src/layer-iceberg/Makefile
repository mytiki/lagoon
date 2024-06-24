.PHONY: clean semver

DIST = ../../../../dist
MODULE = write
PACKAGE = layer-iceberg

semver:
	xmlstarlet ed -L -u "_:project/_:version" -v "$(version)" pom.xml

install:
	mvn install

build: install
	mkdir -p target/lambda/java/lib
	mkdir -p $(DIST)/assets/deploy/$(MODULE)
	cp -r target/write-layer-*.jar target/lambda/java/lib
	cd target/lambda && zip -r layer-content.zip java
	cp target/lambda/layer-content.zip $(DIST)/assets/deploy/$(MODULE)/$(PACKAGE).zip

clean:
	mvn clean
	rm -rf target .aws-sam $(DIST)/assets/deploy/$(MODULE)/$(PACKAGE).zip

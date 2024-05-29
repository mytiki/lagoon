.PHONY: clean semver

semver:
	xmlstarlet ed -L -u "_:project/_:version" -v "$(version)" pom.xml
	sed -i '' 's/SemanticVersion: [0-9]*\.[0-9]*\.[0-9]*/SemanticVersion: $(version)/' template.yml

install:
	mvn install

build: install
	mkdir -p target/lambda/java/lib
	cp -r target/write-layer-*.jar target/lambda/java/lib
	cd target/lambda && zip -r layer-content.zip java
	sam build
	sam validate --lint

publish: build
	sam package --output-template-file .aws-sam/packaged.yaml ;\
	result=$$(sam publish --template .aws-sam/packaged.yaml) ;\
	arn=$$(echo "$$result" | egrep -o 'arn:aws:serverlessrepo:[^ ]+' | head -n 1) ;\
	arn_clean=$$(echo "$$arn" | sed 's/[\"'\'']//g' | tr -d '[:space:]') ;\
	aws serverlessrepo put-application-policy \
		--application-id $$arn_clean \
        --statements Principals=*,PrincipalOrgIDs=$(org),Actions=Deploy,UnshareApplication \
		> /dev/null
clean:
	mvn clean
	rm -rf target

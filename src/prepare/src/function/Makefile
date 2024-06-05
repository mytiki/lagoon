.PHONY: clean semver

semver:
	xmlstarlet ed -L -u "_:project/_:version" -v "$(version)" pom.xml
	xmlstarlet ed -L -u "/_:project/_:dependencies/_:dependency[_:groupId='com.mytiki.lagoon' and _:artifactId='prepare-layer-aws']/_:version" -v "$(version)" pom.xml
	xmlstarlet ed -L -u "/_:project/_:dependencies/_:dependency[_:groupId='com.mytiki.lagoon' and _:artifactId='prepare-layer-spark']/_:version" -v "$(version)" pom.xml
	xmlstarlet ed -L -u "/_:project/_:dependencies/_:dependency[_:groupId='com.mytiki.lagoon' and _:artifactId='prepare-layer-hadoop']/_:version" -v "$(version)" pom.xml
	sed -i.bak 's/SemanticVersion: [0-9]*\.[0-9]*\.[0-9]*/SemanticVersion: $(version)/' template.yml && rm template.yml.bak

compile:
	cd ../layer-aws && make install
	cd ../layer-spark && make install
	cd ../layer-hadoop && make install
	mvn compile

build: compile
	mvn package
	mkdir -p target/lambda/
	cp -r target/prepare-function-*.jar target/lambda/
	mv target/lambda/prepare-function-*.jar target/lambda/prepare-function.jar
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
	rm -rf .aws-sam

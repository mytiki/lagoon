.PHONY: clean semver

RESOURCES = $(wildcard src/*.yml)

semver:
	cd s3_objects && make semver version=$(version)
	sed -i.bak 's/SemanticVersion: [0-9]*\.[0-9]*\.[0-9]*/SemanticVersion: $(version)/' template.yml && rm template.yml.bak

build:
	sam build
	sam validate --lint

publish: build
	cd src/s3_objects && make publish org=$(org)
	sam package --output-template-file .aws-sam/packaged.yaml ;\
	result=$$(sam publish --template .aws-sam/packaged.yaml) ;\
	arn=$$(echo "$$result" | egrep -o 'arn:aws:serverlessrepo:[^ ]+' | head -n 1) ;\
	arn_clean=$$(echo "$$arn" | sed 's/[\"'\'']//g' | tr -d '[:space:]') ;\
	aws serverlessrepo put-application-policy \
		--application-id $$arn_clean \
        --statements Principals=*,PrincipalOrgIDs=$(org),Actions=Deploy,UnshareApplication \
		> /dev/null

clean:
	cd s3_objects && make clean
	rm -rf out
	rm -rf .aws-sam

.PHONY: clean

RESOURCES = $(wildcard src/*.yml src/*/*.yml src/*/*/*.yml)

semver:
	cd src/storage && make publish version=$(version)
	cd src/pipeline && make publish version=$(version)
	cd src/write && make publish version=$(version)
	sed -i '' 's/SemanticVersion: [0-9]*\.[0-9]*\.[0-9]*/SemanticVersion: $(version)/' template.yml

build:
	sam build
	sam validate --lint

publish:
	cd src/write/layer && make publish org=$(org)
	cd src/write/function && make publish org=$(org)
	cd src/storage && make publish org=$(org)
	cd src/pipeline && make publish org=$(org)
	cd src/write && make publish org=$(org)
	sam package --output-template-file .aws-sam/packaged.yaml ;\
	result=$$(sam publish --template .aws-sam/packaged.yaml) ;\
	arn=$$(echo "$$result" | egrep -o 'arn:aws:serverlessrepo:[^ ]+' | head -n 1) ;\
	arn_clean=$$(echo "$$arn" | sed 's/[\"'\'']//g' | tr -d '[:space:]') ;\
	aws serverlessrepo put-application-policy \
		--application-id $$arn_clean \
		--statements Principals=*,PrincipalOrgIDs=$(org),Actions=Deploy,UnshareApplication \
		> /dev/null

clean:
	cd src/storage && make clean
	cd src/pipeline && make clean
	cd src/write && make clean
	rm -rf .aws-sam

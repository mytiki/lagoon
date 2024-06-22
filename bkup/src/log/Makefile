.PHONY: clean semver

RESOURCES = $(wildcard src/*.yml)

semver:
	sed -i.bak 's/SemanticVersion: [0-9]*\.[0-9]*\.[0-9]*/SemanticVersion: $(version)/' template.yml && rm template.yml.bak

compile: template.yml
	@echo "YML files to merge: $(RESOURCES)"
	mkdir -p out
	cp template.yml out/template.yml
	for res in $(RESOURCES); do \
		echo "Processing: $$res"; \
		yq eval '.Resources += load("'$$res'")' out/template.yml -i || exit 1; \
	done

build: compile
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
	rm -rf out
	rm -rf .aws-sam

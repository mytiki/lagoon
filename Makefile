.PHONY: clean

RESOURCES = $(wildcard src/*.yml src/*/*.yml src/*/*/*.yml)

semver:
	cd src/log && make semver version=$(version)
	cd src/storage && make semver version=$(version)
	cd src/pipeline && make semver version=$(version)
	cd src/write && make semver version=$(version)
	cd src/prepare && make semver version=$(version)
	sed -i.bak 's/SemanticVersion: [0-9]*\.[0-9]*\.[0-9]*/SemanticVersion: $(version)/' template.yml && rm template.yml.bak

build:
	sam build
	sam validate --lint

publish:
	cd src/log && make publish org=$(org)
	cd src/storage && make publish org=$(org)
	cd src/pipeline && make publish org=$(org)
	cd src/write && make publish org=$(org)
	cd src/prepare && make publish org=$(org)
	make build
	sam package --output-template-file .aws-sam/packaged.yaml ;\
	result=$$(sam publish --template .aws-sam/packaged.yaml) ;\
	arn=$$(echo "$$result" | egrep -o 'arn:aws:serverlessrepo:[^ ]+' | head -n 1) ;\
	arn_clean=$$(echo "$$arn" | sed 's/[\"'\'']//g' | tr -d '[:space:]') ;\
	aws serverlessrepo put-application-policy \
		--application-id $$arn_clean \
		--statements Principals=*,Actions=Deploy \
		> /dev/null

clean:
	cd src/log && make clean
	cd src/storage && make clean
	cd src/pipeline && make clean
	cd src/write && make clean
	cd src/prepare && make clean
	rm -rf .aws-sam

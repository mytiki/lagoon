.PHONY: clean

RESOURCES = $(wildcard src/*.yml src/*/*.yml)
DIST = ../../dist
MODULE = pipeline
VERSION = $(shell cat ../../version.txt)

compile: template.yml
	sed -i'.bak' '21 s/Default: *.*.*/Default: $(VERSION)/' template.yml && rm template.yml.bak
	sed -i'.bak' '25 s|Default: public.ecr.aws/mytiki/mytiki-lagoon:dbt-example-*.*.*|Default: public.ecr.aws/mytiki/mytiki-lagoon:dbt-example-$(VERSION)|' template.yml && rm template.yml.bak
	echo "YML files to merge: $(RESOURCES)"
	mkdir -p $(DIST)/templates $(DIST)/assets/deploy/$(MODULE)/dagster $(DIST)/assets/deploy/$(MODULE)/dbt
	cp template.yml $(DIST)/templates/$(MODULE).yml
	for res in $(RESOURCES); do \
		echo "Processing: $$res"; \
		yq eval '.Resources += load("'$$res'")' $(DIST)/templates/$(MODULE).yml -i || exit 1; \
	done

build: compile
	sam build --template $(DIST)/templates/$(MODULE).yml
	sam validate --lint --template $(DIST)/templates/$(MODULE).yml
	docker build --tag dagster-$(VERSION) --target dagster --platform linux/amd64 .
	docker build --tag daemon-$(VERSION) --target daemon --platform linux/amd64 .
	cd ../../example/dbt && docker build --tag dbt-example-$(VERSION) --platform linux/amd64 .

publish: clean build
	docker tag dagster-$(VERSION) $(repository):dagster-$(VERSION)
	docker push $(repository):dagster-$(VERSION)
	docker tag daemon-$(VERSION) $(repository):daemon-$(VERSION)
	docker push $(repository):daemon-$(VERSION)
	docker tag dbt-example-$(VERSION) $(repository):dbt-example-$(VERSION)
	docker push $(repository):dbt-example-$(VERSION)

clean:
	rm -rf $(DIST)/templates/$(MODULE).yml -rf .aws-sam
	- docker rmi -f dagster-$(VERSION) daemon-$(VERSION) dbt-$(VERSION)

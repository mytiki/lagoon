.PHONY: clean

RESOURCES = $(wildcard src/*.yml src/*/*.yml)
DIST = ../../dist
MODULE = pipeline

compile: template.yml
	@echo "YML files to merge: $(RESOURCES)"
	mkdir -p $(DIST)/templates $(DIST)/assets/deploy/$(MODULE)/dagster $(DIST)/assets/deploy/$(MODULE)/dbt
	cp template.yml $(DIST)/templates/$(MODULE).yml
	for res in $(RESOURCES); do \
		echo "Processing: $$res"; \
		yq eval '.Resources += load("'$$res'")' $(DIST)/templates/$(MODULE).yml -i || exit 1; \
	done
	cp Dockerfile dagster.yaml workspace.yaml src/daemon/daemon-health.py src/daemon/daemon-start.sh  $(DIST)/assets/deploy/$(MODULE)/dagster
	cp -r ../../example/dbt/ $(DIST)/assets/deploy/$(MODULE)/dbt

build: compile
	sam build --template $(DIST)/templates/$(MODULE).yml
	sam validate --lint --template $(DIST)/templates/$(MODULE).yml

clean:
	rm -rf $(DIST)/templates/$(MODULE).yml -rf .aws-sam

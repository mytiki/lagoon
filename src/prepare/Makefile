.PHONY: clean

RESOURCES = $(wildcard src/*.yml)
DIST = ../../dist
MODULE = prepare

compile: template.yml
	@echo "YML files to merge: $(RESOURCES)"
	mkdir -p $(DIST)/templates $(DIST)/assets/deploy/$(MODULE)
	cp template.yml $(DIST)/templates/$(MODULE).yml
	for res in $(RESOURCES); do \
		echo "Processing: $$res"; \
		yq eval '.Resources += load("'$$res'")' $(DIST)/templates/$(MODULE).yml -i || exit 1; \
	done
	cp src/glue-job.py $(DIST)/assets/deploy/$(MODULE)/glue-job.py

build: compile
	sam build --template $(DIST)/templates/$(MODULE).yml
	sam validate --lint --template $(DIST)/templates/$(MODULE).yml

clean:
	rm -rf $(DIST)/templates/$(MODULE).yml
	rm -rf .aws-sam

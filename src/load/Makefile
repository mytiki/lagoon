.PHONY: clean

RESOURCES = $(wildcard src/*.yml src/*/*.yml)
DIST = ../../dist
MODULE = load
VERSION = $(shell cat ../../version.txt)

compile: template.yml
	sed -i'.bak' '14 s/Default: *.*.*/Default: $(VERSION)/' template.yml && rm template.yml.bak
	echo "YML files to merge: $(RESOURCES)"
	mkdir -p $(DIST)/templates $(DIST)/assets/deploy/$(MODULE)/$(VERSION)
	cp template.yml $(DIST)/templates/$(MODULE).yml
	for res in $(RESOURCES); do \
		echo "Processing: $$res"; \
		yq eval '.Resources += load("'$$res'")' $(DIST)/templates/$(MODULE).yml -i || exit 1; \
	done
	cp src/glue/glue-script.py $(DIST)/assets/deploy/$(MODULE)/$(VERSION)/glue-script.py

build: compile
	sam build --template $(DIST)/templates/$(MODULE).yml
	sam validate --lint --template $(DIST)/templates/$(MODULE).yml

clean:
	rm -rf $(DIST)/templates/$(MODULE).yml .aws-sam

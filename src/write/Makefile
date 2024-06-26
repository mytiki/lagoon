.PHONY: clean semver

RESOURCES = $(wildcard src/*.yml)
DIST = ../../dist
MODULE = write

semver:
	cd src/layer-iceberg && make semver version=$(version)
	cd src/layer-aws && make semver version=$(version)
	cd src/function && make semver version=$(version)

compile: template.yml
	@echo "YML files to merge: $(RESOURCES)"
	mkdir -p $(DIST)/templates $(DIST)/assets/deploy/$(MODULE)
	cp template.yml $(DIST)/templates/$(MODULE).yml
	for res in $(RESOURCES); do \
		echo "Processing: $$res"; \
		yq eval '.Resources += load("'$$res'")' $(DIST)/templates/$(MODULE).yml -i || exit 1; \
	done
	cd src/layer-iceberg && make build
	cd src/layer-aws && make build
	cd src/function && make build

build: compile
	sam build --template $(DIST)/templates/$(MODULE).yml
	sam validate --lint --template $(DIST)/templates/$(MODULE).yml

clean:
	rm -rf $(DIST)/templates/$(MODULE).yml .aws-sam
	cd src/layer-iceberg && make clean
	cd src/layer-aws && make clean
	cd src/function && make clean

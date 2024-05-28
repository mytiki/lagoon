.PHONY: compile clean

RESOURCES = $(wildcard src/*.yml src/*/*.yml src/*/*/*.yml)

compile: template.yml
	@echo "YML files to merge: $(RESOURCES)"
	mkdir -p out
	cp template.yml out/template.yml
	for res in $(RESOURCES); do \
		echo "Processing: $$res"; \
		yq eval '.Resources += load("'$$res'")' out/template.yml -i || exit 1; \
	done

build:
	sam build
	sam validate --lint

clean:
	rm -rf out

.PHONY: clean bump

VERSION = $(shell cat version.txt)

bump:
	echo $(version) > version.txt
	make compile

compile:
	cd cli && sed -i'.bak' '3 s/version = *.*.*/version = "$(VERSION)"/' Cargo.toml && rm Cargo.toml.bak
	cd src/log && make build
	cd src/prepare && make build
	cd src/pipeline && make build
	cd src/write && make build

build: compile
	cd cli && cargo zigbuild --release --target=x86_64-unknown-linux-musl
	docker build --tag cli-$(VERSION) .

publish: clean build
	cd src/pipeline && make publish
	docker tag cli-$(VERSION) $(repository):$(VERSION)
	docker push $(repository):$(VERSION)

clean:
	cd src/log && make clean
	cd src/prepare && make clean
	cd src/pipeline && make clean
	cd src/write && make clean
	rm -rf dist

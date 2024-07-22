.PHONY: clean bump

VERSION = $(shell cat version.txt)

bump:
	echo $(version) > version.txt
	make compile

compile:
	cd cli && sed -i'.bak' '3 s/version = *.*.*/version = "$(VERSION)"/' Cargo.toml && rm Cargo.toml.bak
	cd src/pipeline && make build
	cd src/load && make build

build: compile
	cd cli && cargo zigbuild --release --target=x86_64-unknown-linux-musl
	docker build --tag cli-$(VERSION) .

publish: clean build
	cd src/pipeline && make publish
	docker tag cli-$(VERSION) $(repository):$(VERSION)
	docker push $(repository):$(VERSION)

clean:
	cd src/pipeline && make clean
	cd src/load && make clean
	rm -rf dist
	- docker rmi -f cli-$(VERSION)

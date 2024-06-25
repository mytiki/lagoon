.PHONY: clean semver

semver:
	cd src/write && make semver version=$(version)

compile:
	cd src/log && make build
	cd src/prepare && make build
	cd src/pipeline && make build
	cd src/write && make build

build: compile
	cd cli && cargo zigbuild --release --target=x86_64-unknown-linux-musl
	docker build --tag mytiki-lagoon .

publish: clean
	make semver version=$(version)
	make build
	docker tag mytiki-lagoon $(account).dkr.ecr.$(region).amazonaws.com/mytiki-lagoon:$(version)
	aws ecr get-login-password --region $(region) | docker login --username AWS --password-stdin $(account).dkr.ecr.$(region).amazonaws.com
	docker push $(account).dkr.ecr.$(region).amazonaws.com/mytiki-lagoon:$(version)

clean:
	cd src/log && make clean
	cd src/prepare && make clean
	cd src/pipeline && make clean
	cd src/write && make clean
	rm -rf dist

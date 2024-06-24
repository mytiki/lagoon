.PHONY: clean semver

semver:
	cd src/write && make semver version=$(version)

build:
	cd src/log && make build
	cd src/prepare && make build
	cd src/pipeline && make build
	cd src/write && make build

clean:
	cd src/log && make clean
	cd src/prepare && make clean
	cd src/pipeline && make clean
	cd src/write && make clean
	rm -rf dist

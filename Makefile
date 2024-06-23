.PHONY: clean

build:
	cd src/log && make build
	cd src/prepare && make build

clean:
	cd src/log && make clean
	cd src/prepare && make clean
	rm -rf dist

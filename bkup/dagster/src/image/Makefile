.PHONY: clean semver

build:
	sam build
	sam validate --lint
	docker build --platform="linux/amd64" --target webserver --tag $(account).dkr.ecr.$(region).amazonaws.com/mytiki-lagoon-pipeline:dagster-0.0.1 .
	docker build --platform="linux/amd64" --target daemon --tag $(account).dkr.ecr.$(region).amazonaws.com/mytiki-lagoon-pipeline:daemon-0.0.1 .

deploy:
	sam deploy --no-fail-on-empty-changeset
	aws ecr get-login-password --region $(region) | docker login --username AWS --password-stdin $(account).dkr.ecr.$(region).amazonaws.com
	docker push $(account).dkr.ecr.$(region).amazonaws.com/mytiki-lagoon-pipeline:dagster-0.0.1
	docker push $(account).dkr.ecr.$(region).amazonaws.com/mytiki-lagoon-pipeline:daemon-0.0.1

clean:
	rm -rf out
	rm -rf .aws-sam

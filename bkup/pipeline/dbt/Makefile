.PHONY: clean semver

build:
	sam build
	sam validate --lint
	docker build --platform="linux/amd64" --tag $(account).dkr.ecr.$(region).amazonaws.com/mytiki-lagoon-pipeline:dbt-default .

deploy:
#	sam deploy --no-fail-on-empty-changeset
	aws ecr get-login-password --region $(region) | docker login --username AWS --password-stdin $(account).dkr.ecr.$(region).amazonaws.com
	docker push $(account).dkr.ecr.$(region).amazonaws.com/mytiki-lagoon-pipeline:dbt-default

clean:
	rm -rf out
	rm -rf .aws-sam

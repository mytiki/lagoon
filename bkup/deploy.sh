#!/bin/bash

echo "Enter your AWS profile (or press enter to use 'default'):"
read profile
if [ -z "$profile" ]; then
  profile="default"
fi

echo "Enter your mytiki.com access role ARN:"
read role_arn

echo "Deploying Lagoon. This will take a couple of minutes.."
sar_name="mytiki-lagoon"
stack_name=serverlessrepo-$sar_name
application_id="arn:aws:serverlessrepo:us-east-2:992382831795:applications/mytiki-lagoon"

stack_exists=$(aws cloudformation describe-stacks \
  --stack-name $stack_name \
  --profile "$profile" 2>/dev/null)

change_set=$(aws serverlessrepo create-cloud-formation-change-set \
  --application-id $application_id \
  --stack-name $sar_name \
  --capabilities CAPABILITY_IAM CAPABILITY_AUTO_EXPAND CAPABILITY_NAMED_IAM CAPABILITY_RESOURCE_POLICY \
  --parameter-overrides '[{"Name":"AccessRole","Value":"'"$role_arn"'"}]' \
  --profile "$profile" \
  --query 'ChangeSetId' \
  --output text)

sleep 60

aws cloudformation execute-change-set \
  --change-set-name "$change_set" \
  --stack-name $stack_name \
  --profile "$profile"

if [ -z "$stack_exists" ]; then
  aws cloudformation wait stack-create-complete \
    --stack-name $stack_name \
    --profile "$profile"
else
  aws cloudformation wait stack-update-complete \
    --stack-name $stack_name \
    --profile "$profile"
fi

bucket=$(aws cloudformation describe-stacks \
  --stack-name $stack_name \
  --profile "$profile" \
  --query "Stacks[0].Outputs[?OutputKey=='Location'].OutputValue" \
  --output text)

## Copy the assets folder to the S3 bucket
echo "Deployed. Adding required assets..."
temp_dir="tmp"
github_url="https://github.com/mytiki/lagoon/archive/refs/heads/main.zip"
mkdir -p $temp_dir
wget $github_url -O $temp_dir/repo.zip -q
unzip -q $temp_dir/repo.zip -d $temp_dir
aws s3 cp $temp_dir/lagoon-main/assets "$bucket/assets" \
  --recursive \
  --profile "$profile" > /dev/null 2>&1
rm -rf $temp_dir

echo "Lagoon ready: $bucket"

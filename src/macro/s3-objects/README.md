# S3 Objects

The S3Objects macro adds a new resource type: AWS::S3::Object which you can use to populate an S3 bucket.

You can either create new S3 objects or copy S3 buckets from other buckets that you have permissions to access.

As with any other CloudFormation resource, if you delete a stack containing S3 objects defined with this macro, those objects will be deleted.

A typical use case for this macro might be, for example, to populate an S3 website with static assets.

**Modified from [aws-cloudformation/aws-cloudformation-templates/CloudFormation/MacrosExamples/S3Objects](https://github.com/aws-cloudformation/aws-cloudformation-templates/blob/main/CloudFormation/MacrosExamples/S3Objects/README.md) under Apache License 2.0**


## Usage
```yaml
Transform: S3Objects
Resources:
  Bucket:
    Type: AWS::S3::Bucket

  Object:
    Type: AWS::S3::Object
    Properties:
      Target:
        Bucket: !Ref Bucket
        Key: README.md
        ContentType: text/plain
      Body: Hello, world!
```

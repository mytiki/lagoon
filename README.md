<p align="center">
  <a href="https://github.com/mytiki/platform">
    <picture>
      <source media="(prefers-color-scheme: dark)" srcset="https://github.com/mytiki/.github/assets/3769672/931b81d7-0359-4e3c-8f86-5b34e5e24d57">
      <source media="(prefers-color-scheme: light)" srcset="https://github.com/mytiki/.github/assets/3769672/10278053-ec4d-40d1-a778-dd03a7d36c95">
      <img alt="mytiki logo" src="https://github.com/mytiki/.github/assets/3769672/10278053-ec4d-40d1-a778-dd03a7d36c95" height="50">
    </picture>
  </a>
</p>

# Lagoon
Create consumption-ready data. Aggregate raw application data in an Iceberg data lake, transform with dbt, and enforce compliance requirements. Use with [mytiki.com](https://mytiki.com) to monetize your new data assets.

[![version](https://img.shields.io/github/v/release/mytiki/lagoon?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mytiki/lagoon/releases/latest)
[![deploy](https://img.shields.io/badge/AWS-DEPLOY-FD8E31?style=for-the-badge&logo=amazon-aws&logoColor=white)](https://us-east-2.console.aws.amazon.com/lambda/home#/create/app?applicationId=arn:aws:serverlessrepo:us-east-2:992382831795:applications/mytiki-lagoon)
[![docs](https://img.shields.io/badge/GET%20STARTED-DOCS-FFE68F?style=for-the-badge&logo=readme&logoColor=white)](https://docs.mytiki.com/docs/productization-overview)

### Quick Start

This guide is for self-hosting a Lagoon in your own AWS account. For our managed service, just book a [call with our team](https://cal.com/team/tiki/beta).

1. **[Request an Access Role](https://cal.com/team/tiki/beta)** for **free** from our team. 
2. **[Open the SAR application](https://us-east-2.console.aws.amazon.com/lambda/home#/create/app?applicationId=arn:aws:serverlessrepo:us-east-2:992382831795:applications/mytiki-lagoon)** in your AWS account. 
3. Enter the Access Role ARN in the `AccessRole` field and click **Deploy**.

Done! Look for the `serverlessrepo-mytiki-lagoon` stack in your CloudFormation console. The main entry point for interacting with your Lagoon is the S3 bucket named `mytiki-lagoon-<account-id>-<region>`.

## About

The Lagoon project was created as a result of the [mytiki.com](https://mytiki.com) team's work (and frustrations) with the infrastructure and management required to productize data —taking large, raw application datasets and transforming them for consumption by other teams. The project is based on the original internal mytiki.com platform built to coalesce data from dozens of applications, billions of records, and millions of users into standardized clean assets for consumption by external data teams and companies. Along the way we kept hearing from data teams struggling with the same problems as us —it's hard and expensive.

Lagoon is a blend of popular open source tools, managed infrastructure (AWS), wiring, and simplified interfaces to modularize data productization. In short, if you have a lot of data, a small team, a limited budget, and you need to continuously deliver good clean structured data, [deploy a Lagoon](https://us-east-2.console.aws.amazon.com/lambda/home#/create/app?applicationId=arn:aws:serverlessrepo:us-east-2:992382831795:applications/mytiki-lagoon).

To lean more about the Lagoon project and how it works, please visit our [documentation](https://docs.mytiki.com/docs/productization-overview).

### Project Status
[![tests](https://img.shields.io/github/actions/workflow/status/mytiki/lagoon/test.yml?style=for-the-badge&logo=github&logoColor=white&label=TESTS)](https://github.com/mytiki/lagoon/actions/workflows/test.yml)
[![milestone](https://img.shields.io/github/milestones/progress/mytiki/lagoon/2?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mytiki/lagoon/milestones)

Lagoon is under active development by the [mytiki.com](https://mytiki.com) team. We are currently in the process of open-sourcing and adding in the relevant components of our internal platform. Stay tuned.

#### Upcoming Features
- [ ] v0.3.0 — Bulk import flat files from common formats like JSON, CSV, and Avro.
- [ ] v0.4.0 — Automate execution of [dbt](https://github.com/dbt-labs/dbt-core) models with [Dagster](https://github.com/dagster-io/dagster) orchestration
- [ ] v0.5.0 — Asynchronously stream in records via a REST API

## Collaboration
[![discussions](https://img.shields.io/github/discussions/mytiki/.github?style=for-the-badge&logo=github&logoColor=white)](https://github.com/orgs/mytiki/discussions)
[![issues](https://img.shields.io/github/issues/mytiki/lagoon?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mytiki/lagoon/issues)

Please review our [Contributor Guidelines](https://github.com/mytiki/.github/blob/main/CONTRIBUTING.md) before beginning. Utilize [GitHub Issues](https://github.com/mytiki/lagoon/issues/new/choose), [Discussions](https://github.com/orgs/mytiki/discussions), and [Security Advisories](https://github.com/mytiki/lagoon/security/advisories/new) to provide feedback. Fork and create a pull request to contribute code. Always include units tests and documentation for submitted changes.

### Building

Lagoon is polyglot. It's mostly [Java 21](https://docs.aws.amazon.com/corretto/latest/corretto-21-ug/downloads-list.html) and [AWS SAM/Cloudformation](https://aws.amazon.com/serverless/sam/), with some [Rust](https://www.rust-lang.org/tools/install), and a bit of [Python](https://www.python.org/downloads/).

In addition, you'll need:
- [Maven](https://maven.apache.org/download.cgi)
- [Cargo](https://doc.rust-lang.org/cargo/getting-started/installation.html)
- [AWS CLI](https://aws.amazon.com/cli/)
- [SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/serverless-sam-cli-install.html)

Tests, versioning, and releases are automated with GitHub Actions. Please use [conventional commits](https://www.conventionalcommits.org/en/v1.0.0/). 

#### Open Source Projects

Lagoon depends on a number of open source projects, selected for a balance of functionality, performance, and community support such as:

- [Iceberg](https://iceberg.apache.org/)
- [dbt](https://github.com/dbt-labs/dbt-core)
- [Dagster](https://github.com/dagster-io/dagster)
- [Parquet](https://parquet.apache.org/)
- [Hadoop](https://hadoop.apache.org/)
- [Avro](https://avro.apache.org/)


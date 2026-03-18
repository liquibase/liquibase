# Liquibase [![Build and Test](https://github.com/liquibase/liquibase/actions/workflows/run-tests.yml/badge.svg)](https://github.com/liquibase/liquibase/actions/workflows/run-tests.yml) [![Nightly Release](https://github.com/liquibase/liquibase/actions/workflows/nightly-release.yml/badge.svg)](https://github.com/liquibase/liquibase/releases/tag/nightly) [![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=liquibase&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=liquibase)
<p align="center"><img src="https://github.com/liquibase/liquibase/blob/master/Liquibase.png" width="30%" height="30%"></p>

Liquibase helps millions of developers track, version, and deploy database schema changes. It will help you to:
- Control database schema changes for specific versions
- Eliminate errors and delays when releasing databases
- Automatically order scripts for deployment
- Easily rollback changes
- Collaborate with tools you already use

This repository contains the main source code for Liquibase Community. For more information about the product, see the [Liquibase website](https://www.liquibase.com/).

## Release Cadence and Access

At Liquibase, we believe great database change management starts with a community that can rely on a predictable and transparent release process. To support this commitment, and starting with version 5.0.2, we are providing two clear ways for the community to access updates and improvements:

- **Main Branch Builds on GitHub** – Main branch builds are continuously published on GitHub, offering early access to the latest improvements and fixes as they are developed. These builds enable the community to test upcoming capabilities and provide feedback ahead of the next official release.
- **Quarterly Community Releases** – Liquibase Community updates are released quarterly (Feb, May, Aug, Nov) providing stable, production-ready versions that allow teams to plan upgrades and maintenance with confidence. These releases are available through all standard distribution channels, including GitHub, Maven Central, package managers, container registries, and other official Liquibase Community distribution locations.

Together, these options ensure that teams can choose the path that best fits their needs - whether prioritizing stability through scheduled releases or engaging early with the latest innovations in Liquibase.

### Build Access and Release Planning

- **Nightly builds:** The latest Main branch build is available at https://github.com/liquibase/liquibase/releases/tag/nightly — download `liquibase-nightly.tar.gz` (Linux/macOS) or `liquibase-nightly.zip` (Windows). Updated automatically after each successful test run on master.
- **Latest Release: v5.0.2 — March 5, 2026** \
https://www.liquibase.com/download-community
- **Next Planned Release: May 15, 2026** 
- **Roadmap:** \
https://github.com/orgs/liquibase/projects/3/views/9?layout=board

## Liquibase Automation and Integrations

Liquibase Community has built-in support for a variety of databases. Databases that are not part of Liquibase Community require extensions that you can download for free. Here is the full list of [supported databases](https://www.liquibase.com/supported-databases).

Liquibase can be integrated with Maven, Ant, Gradle, Spring Boot, and other CI/CD tools. For a full list, see [Liquibase Tools & Integrations](https://docs.liquibase.com/tools-integrations/home.html). You can use Liquibase with [GitHub Actions](https://github.com/liquibase/liquibase-github-action-example), [Spinnaker](https://github.com/liquibase/liquibase-spinnaker-plugin), and many different [workflows](https://docs.liquibase.com/workflows/home.html).


## Install and Run Liquibase

### System Requirements
Liquibase system requirements can be found on the [Download Liquibase](https://www.liquibase.com/download) page.

### An H2 in-memory database example for CLI
1. [Download and run the appropriate installer](https://www.liquibase.com/download). 
2. Make sure to add Liquibase to your PATH.
3. Copy the included `examples` directory to the needed location.
4. Open your CLI and navigate to your `examples/sql` or `examples/xml` directory.
5. Start the included H2 database with the `liquibase init start-h2` command.
6. Run the `liquibase update` command.
7. Run the `liquibase history` command to see what has executed!

See also how to [get started with Liquibase in minutes](https://docs.liquibase.com/start/home.html) or refer to our [Installing Liquibase](https://docs.liquibase.com/start/install/home.html) documentation page for more details.

## Documentation

Visit the [Liquibase Documentation](https://docs.liquibase.com/home.html) website to find the information on how Liquibase works.

## Courses

Learn all about Liquibase by taking our free online courses at [Liquibase University](https://learn.liquibase.com/).

## Want to help?

Want to file a bug or improve documentation? Excellent! Read up on our guidelines for [contributing](https://contribute.liquibase.com/)!

### Contribute code 

Use our [step-by-step instructions](https://contribute.liquibase.com/code/) for contributing code to the Liquibase project. 

### Join the Liquibase Community

Earn points for your achievements and contributions, collect and show off your badges, add accreditations to your LinkedIn. [Learn more about the pathway to Legend and benefits](https://www.liquibase.com/community/liquibase-legends). Enjoy being part of the community!

## Liquibase Extensions

[Provide more database support and features for Liquibase](https://contribute.liquibase.com/extensions-integrations/directory/).

## License

Liquibase Community is [licensed under the Functional Source License (FSL)](https://fsl.software/FSL-1.1-ALv2.template.md).

[Liquibase Secure](https://www.liquibase.com/liquibase-secure) has additional features and support and is commercially licensed.

LIQUIBASE is a registered trademark of [Liquibase Inc.](https://www.liquibase.com/company)

## [Contact us](https://www.liquibase.com/contact)

[Liquibase Forum](https://forum.liquibase.org/) 

[Liquibase Blog](https://www.liquibase.com/blog)

[Get Support & Advanced Features](https://www.liquibase.com/pricing)

## Publish Release Manual Trigger to Sonatype 

1. When a PO (Product Owner) or a Team Leader navigates to Publish a release from here -> https://github.com/liquibase/liquibase/releases/, the workflow from /workflow/release-published.yml job is triggered. 
2. When a release is triggered, the workflow file will stop after `Setup` step and an email will be sent out to the list of `approvers` mentioned in job `manual_trigger_deployment`. You can click on the link and perform anyone of the options mentioned in description. 
3. A minimum of 2 approvers are needed in order for the other jobs such as `deploy_maven`, `deploy_javadocs`, `publish_to_github_packages`, etc to be executed.
4. When you view the GitHub PR, make sure to verify the version which is being published. It should say something like `Deploying v4.20.0 to sonatype`




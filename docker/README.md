# Official Liquibase Docker Images

## 🚨 Important: Liquibase 5.0 Changes 🚨

### Liquibase Community vs Liquibase Secure

Starting with **Liquibase 5.0**, we have introduced a clear separation between our open source Community edition and our commercial Secure offering:

- **`liquibase/liquibase`** (Community Edition): Community version under the Functional Source License (FSL)
- **`liquibase/liquibase-secure`** (Secure Edition): Commercial version with enterprise features

**If you have a valid Liquibase License Key, you should now use `liquibase/liquibase-secure` instead of `liquibase/liquibase`.**

### 📋 Image Availability Matrix

| Version Range | Community Image                                 | Secure Image                 | License                 | Docker Official               |
| ------------- | ----------------------------------------------- | ---------------------------- | ----------------------- | ----------------------------- |
| **5.0+**      | `liquibase/liquibase`                           | `liquibase/liquibase-secure` | FSL\* / Commercial      | ❌ No\*\*                     |
| **4.x**       | `liquibase:4.x`\*\*\*<br/>`liquibase/liquibase` | `liquibase/liquibase-secure` | Apache 2.0 / Commercial | ✅ Yes\*\*\* (Community only) |

- \*FSL = Functional Source License (See [Liquibase License Information](#license-information))
- \*\*For Liquibase 5.0+, use the community registry image `liquibase/liquibase` (not available as official Docker image).
- \*\*\*Liquibase 4 community image is available as the official Docker image at [https://hub.docker.com/\_/liquibase](https://hub.docker.com/_/liquibase). Pull using `docker pull liquibase:4.x`.

### 🚨 Breaking Change: Drivers and Extensions No Longer Included

As of **Liquibase 5.0**, the Community edition (`liquibase/liquibase`) and the official Docker Community liquibase image **no longer include database drivers or extensions by default**.

**What this means for you:**

- You must now explicitly add database drivers using the Liquibase Package Manager (LPM)
- Extensions must be manually installed or mounted into the container
- MySQL driver installation via `INSTALL_MYSQL=true` environment variable is still supported

**Learn more:** [Liquibase 5.0 Release Announcement](https://www.liquibase.com/blog/liquibase-5-0-release)

### Adding Drivers with LPM

```dockerfile
FROM liquibase/liquibase:latest
# Add database drivers as needed
RUN lpm add mysql --global
RUN lpm add postgresql --global
RUN lpm add mssql --global
```

---

## 🌍 Available Registries

We publish Liquibase images to multiple registries for flexibility:

| Registry                      | Community Image                      | Secure Image                                |
| ----------------------------- | ------------------------------------ | ------------------------------------------- |
| **Docker Hub (default)**      | `liquibase/liquibase`                | `liquibase/liquibase-secure`                |
| **GitHub Container Registry** | `ghcr.io/liquibase/liquibase`        | `ghcr.io/liquibase/liquibase-secure`        |
| **Amazon ECR Public**         | `public.ecr.aws/liquibase/liquibase` | `public.ecr.aws/liquibase/liquibase-secure` |

## 🚀 Quick Start

### For Community Users (Liquibase 5.0+)

```bash
# Pull the community image
docker pull liquibase/liquibase:5.0.1

# Run with a changelog
docker run --rm \
  -v /path/to/changelog:/liquibase/changelog \
  -e LIQUIBASE_COMMAND_URL="jdbc:postgresql://localhost:5432/mydb" \
  -e LIQUIBASE_COMMAND_USERNAME="username" \
  -e LIQUIBASE_COMMAND_PASSWORD="password" \
  liquibase/liquibase update
```

### For Secure Edition Users

```bash
# Pull the secure image
docker pull liquibase/liquibase-secure:5.0.1

# Run with a changelog and license key
docker run --rm \
  -v /path/to/changelog:/liquibase/changelog \
  -e LIQUIBASE_COMMAND_URL="jdbc:postgresql://localhost:5432/mydb" \
  -e LIQUIBASE_COMMAND_USERNAME="username" \
  -e LIQUIBASE_COMMAND_PASSWORD="password" \
  -e LIQUIBASE_LICENSE_KEY="your-license-key" \
  liquibase/liquibase-secure:5.0.1 update
```

### For Liquibase 4 Users

If you're still using Liquibase 4, you can pull from either the official Docker repository or the community registry:

**Official Docker Repository:**

```bash
# Pull the latest Liquibase 4 image
docker pull liquibase:latest

# Or pull a specific version
docker pull liquibase:4.x
```

**Community Registry:**

```bash
# Pull from community registry
docker pull liquibase/liquibase:4.x
```

---

## 📖 Upgrading from Liquibase 4 to 5.0

If you're upgrading from Liquibase 4 to 5.0, follow these steps:

### Step 1: Understand License Requirements

- **Liquibase 4**: Uses Apache 2.0 license (always available)
- **Liquibase 5.0 Community**: Uses Functional Source License (FSL)
- **Liquibase 5.0 Secure**: Requires a commercial license

Read more: [Liquibase License Information](#license-information)

### Step 2: Determine Which Edition You Need

**Use Community Edition if:**

- You are an open source user
- You accept the Functional Source License terms
- You do not require enterprise features

**Use Secure Edition if:**

- You have a commercial Liquibase license
- You need enterprise features like Policy Checks, Quality Checks, or Advanced Rollback
- Your organization requires commercial support

### Step 3: Update Your Image Reference

**If using Community Edition:**

```bash
# Before (Liquibase 4)
FROM liquibase/liquibase:4.x

# After (Liquibase 5.0+)
FROM liquibase/liquibase:5.0  # or :latest
```

**If using PRO Edition:**

```bash
# Before (Liquibase 4)
FROM liquibase/liquibase-pro:4.x

# After (Liquibase 5.0+)
FROM liquibase/liquibase-secure:5.0  # or :latest
```

### Step 4: Update Driver Installation

**Liquibase 5.0+ no longer includes drivers by default.** Add drivers explicitly:

```dockerfile
FROM liquibase/liquibase:latest

# Add required database drivers
RUN lpm add postgresql --global
RUN lpm add mysql --global
RUN lpm add mssql --global
```

Or at runtime using environment variables:

```bash
docker run -e INSTALL_MYSQL=true liquibase/liquibase:latest update
```

### Step 5: Test in Non-Production First

```bash
# Test your changelogs against a test database
docker run --rm \
  -v /path/to/changelog:/liquibase/changelog \
  -e LIQUIBASE_COMMAND_URL="jdbc:postgresql://test-db:5432/testdb" \
  -e LIQUIBASE_COMMAND_USERNAME="username" \
  -e LIQUIBASE_COMMAND_PASSWORD="password" \
  liquibase/liquibase:5.0 validate
```

### Step 6: Complete Production Migration

Once testing is successful, update your production deployments to use the new image.

---

## 🔐 License Information

### Functional Source License (FSL) - Liquibase 5.0 Community

The Liquibase 5.0 Community edition is available under the Functional Source License (FSL). This license:

- Allows you to freely use Liquibase for database migrations
- Prohibits commercial use that competes with Liquibase's products or services
- Automatically transitions to the Apache 2.0 license after two years
- Provides full source code access (but not OSI-approved open source)

Read the full license: [Functional Source License on fsl.software](https://fsl.software/)

### Apache 2.0 License - Liquibase 4

Liquibase 4 versions continue to use the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0).

### Commercial License - Liquibase Secure

The [Liquibase Secure edition](https://www.liquibase.com/liquibase-secure) requires a commercial license and provides enterprise-grade database change management with advanced capabilities:

**Developer Productivity:**
- VS Code Extension for IDE-native operations
- Liquibase Flows for environment consistency
- Policy Checks to enforce standards and block risky changes
- Support for 60+ database types

**Secure Automation:**
- CI/CD deployment automation with policy enforcement
- Targeted rollbacks for precise recovery
- Advanced drift detection and alerting
- Secrets management and RBAC enforcement
- Multi-environment deployment capabilities

**Change Insights & Compliance:**
- Unified change tracking with full audit context
- Real-time drift detection
- Audit-ready compliance reporting (SOX, HIPAA, PCI, SOC2, GDPR)
- Risk scoring for change assessment

For more information and licensing inquiries, visit [liquibase.com/get-liquibase](https://www.liquibase.com/get-liquibase)

---

## 🔒 Verifying Secure Images

Liquibase Secure images include supply chain security features for compliance with SLSA Level 3 requirements. These features help verify image authenticity and provide transparency into image contents.

### Verify Image Signature

Liquibase Secure images are signed using [Cosign](https://docs.sigstore.dev/cosign/overview/) with keyless signing via GitHub OIDC. To verify a signature:

```bash
# Install cosign: https://docs.sigstore.dev/cosign/installation/
cosign verify liquibase/liquibase-secure:latest \
  --certificate-oidc-issuer=https://token.actions.githubusercontent.com \
  --certificate-identity-regexp="https://github.com/liquibase/liquibase/.*"
```

### View SBOM (Software Bill of Materials)

Each Liquibase Secure image includes an SBOM attestation listing all components:

```bash
docker buildx imagetools inspect liquibase/liquibase-secure:latest --format '{{ json .SBOM }}'
```

### View Build Provenance

Build provenance attestations provide details about how the image was built:

```bash
docker buildx imagetools inspect liquibase/liquibase-secure:latest --format '{{ json .Provenance }}'
```

> **Note:** These supply chain security features are only available for Liquibase Secure images, not the Community edition.

---

## 🛡️ Vulnerability Scanning

Published Liquibase Docker images (Community and Secure) are automatically scanned for known vulnerabilities using multiple security scanners. Scans run Monday through Friday at 10 AM UTC and cover the most recent tags of each image (up to 10 per repository by default).

### What Gets Scanned

| Scan | Scanner | Coverage |
|------|---------|----------|
| OS & Application Libraries | Trivy | Operating system packages and top-level Java libraries |
| Nested JAR Dependencies | Trivy | Libraries bundled inside Liquibase JARs |
| SBOM-based Scan | Grype | Full Software Bill of Materials analysis |

### Viewing Scan Results

**Security Dashboard** — [Liquibase Security](https://security.liquibase.com/docker) provides an interactive interface to explore vulnerability scan results across all image versions:

- **Image Overview** — Browse all scanned versions with severity breakdowns, CVSS trends, and total counts
- **Version Detail** — View every CVE in a specific image version, filterable by severity and component type (OS, JRE, JAR, Driver), with upgrade recommendations
- **Version Compare** — Compare two versions side by side to see which CVEs were fixed, which are new, and which are shared
- **Export** — Download vulnerability data as CSV or print reports as PDF

**GitHub Actions** — Raw scan results are also available directly from this repository:
1. Go to the **Actions** tab
2. Select **Published Images Vulnerability Scanning**
3. Choose a workflow run to view the summary or download artifacts

For a detailed guide on reading vulnerability reports, see [SECURITY.md](SECURITY.md).

---

## Dockerfile

```dockerfile
FROM liquibase/liquibase:latest
# OR ghcr.io/liquibase/liquibase:latest    # GHCR
# OR public.ecr.aws/liquibase/liquibase:latest   # Amazon ECR Public
```

## Scripts

### Community Edition

```bash
# Docker Hub (default)
docker pull liquibase/liquibase

# GitHub Container Registry
docker pull ghcr.io/liquibase/liquibase

# Amazon ECR Public
docker pull public.ecr.aws/liquibase/liquibase
```

### Liquibase Secure Edition

```bash
# Docker Hub (default)
docker pull liquibase/liquibase-secure

# GitHub Container Registry
docker pull ghcr.io/liquibase/liquibase-secure

# Amazon ECR Public
docker pull public.ecr.aws/liquibase/liquibase-secure
```

### Pulling the Latest or Specific Version

#### Community Edition

```bash
# Latest
docker pull liquibase/liquibase:latest
docker pull ghcr.io/liquibase/liquibase:latest
docker pull public.ecr.aws/liquibase/liquibase:latest

# Specific version (example: 4.32.0)
docker pull liquibase/liquibase:4.32.0
docker pull ghcr.io/liquibase/liquibase:4.32.0
docker pull public.ecr.aws/liquibase/liquibase:4.32.0
```

#### Liquibase Secure Edition

```bash
# Latest
docker pull liquibase/liquibase-secure:latest
docker pull ghcr.io/liquibase/liquibase-secure:latest
docker pull public.ecr.aws/liquibase/liquibase-secure:latest

# Specific version (example: 4.32.0)
docker pull liquibase/liquibase-secure:4.32.0
docker pull ghcr.io/liquibase/liquibase-secure:4.32.0
docker pull public.ecr.aws/liquibase/liquibase-secure:4.32.0
```

For any questions or support, please visit our [Liquibase Community Forum](https://forum.liquibase.org/).

---

This is the community repository for [Liquibase](https://download.liquibase.org/) images.

## 🚨 BREAKING CHANGE

Support for Snowflake database has been moved from the external extension liquibase-snowflake into the main Liquibase artifact. This means that Snowflake is now included in the main docker image. If you are using the snowflake extension, remove it from your lib directory or however you are including it in your project. If you are using the Docker image, use the main v4.12+ as there will no longer be a snowflake separate docker image produced. The latest separate Snowflake image will be v4.11. You need to update your reference to either latest to use the main one that includes Snowflake or the version tag you prefer. <https://github.com/liquibase/liquibase/pull/2841>

## 🏷️ Image Tags and Versions

Liquibase Docker images use semantic versioning with the following tag strategies:

### Tag Formats

| Tag Format         | Example                             | Description                    |
| ------------------ | ----------------------------------- | ------------------------------ |
| `latest`           | `liquibase/liquibase:latest`        | Latest stable release          |
| `latest-alpine`    | `liquibase/liquibase:latest-alpine` | Latest stable Alpine variant   |
| `<version>`        | `liquibase/liquibase:5.0.0`         | Specific version (exact match) |
| `<version>-alpine` | `liquibase/liquibase:5.0.0-alpine`  | Specific Alpine version        |
| `<major>.<minor>`  | `liquibase/liquibase:5.0`           | Latest patch for major.minor   |

### Community vs Secure Image Tags

The same tag structure applies to both image types:

- **Community**: `liquibase/liquibase:5.0.0`
- **Secure**: `liquibase/liquibase-secure:5.0.0`

Both are available across all registries (Docker Hub, GHCR, Amazon ECR Public).

### Supported Tags

The following tags are officially supported and can be found on [Docker Hub](https://hub.docker.com/r/liquibase/liquibase/tags):

**Community Image:**

- `liquibase/liquibase:latest` - Latest 5.0+ release
- `liquibase/liquibase:5.0` - Latest 5.0.x release
- `liquibase/liquibase:latest-alpine` - Latest Alpine variant
- `liquibase/liquibase:4.x` - Liquibase 4 versions (Apache 2.0)

**Secure Image:**

- `liquibase/liquibase-secure:latest` - Latest Secure release
- `liquibase/liquibase-secure:5.0` - Latest 5.0.x release
- `liquibase/liquibase-secure:latest-alpine` - Latest Secure Alpine variant

### Choosing the Right Tag

- **For production**: Use major.minor tags (e.g., `5.0`) for reproducibility with latest patches
- **For development**: Use `latest` or `latest-alpine` for convenience
- **For Alpine Linux**: Append `-alpine` for smaller image size
- **For Liquibase 4**: Use `4.x` versions (Apache 2.0 license)

## 📦 Using the Docker Image

### 🏷️ Standard Image

The `liquibase/liquibase:<version>` image is the standard choice. Use it as a disposable container or a foundational building block for other images.

For examples of extending the standard image, see the [standard image examples](https://github.com/liquibase/liquibase/tree/master/docker/examples).

### 🏷️ Alpine Image

The `liquibase/liquibase:<version>-alpine` image is a lightweight version designed for environments with limited resources. It is built on Alpine Linux and has a smaller footprint.

For examples of extending the alpine image, see the [alpine image examples](https://github.com/liquibase/liquibase/tree/master/docker/examples).

### 🐳 Docker Compose Example

For a complete example using Docker Compose with PostgreSQL, see the [docker-compose example](https://github.com/liquibase/liquibase/tree/master/docker/examples/docker-compose).

### 📄 Using the Changelog File

Mount your changelog directory to the `/liquibase/changelog` volume and use relative paths for the `--changeLogFile` argument.

#### Example

```shell
docker run --rm -v /path/to/changelog:/liquibase/changelog liquibase/liquibase --changeLogFile=changelog.xml update
```

### 🔄 CLI-Docker Compatibility

Starting with this version, Docker containers now behave consistently with CLI usage for file path handling. When you mount your changelog directory to `/liquibase/changelog`, the container automatically changes its working directory to match, making relative file paths work the same way in both CLI and Docker environments.

**Before this enhancement:**

- CLI: `liquibase generateChangeLog --changelogFile=mychangelog.xml` (creates file in current directory)
- Docker: `liquibase generateChangeLog --changelogFile=changelog/mychangelog.xml` (had to include path prefix)

**Now (improved):**

- CLI: `liquibase generateChangeLog --changelogFile=mychangelog.xml` (creates file in current directory)
- Docker: `liquibase generateChangeLog --changelogFile=mychangelog.xml` (creates file in mounted changelog directory)

Both approaches now work identically, making it easier to switch between local CLI and CI/CD Docker usage without modifying your commands or file paths.

#### How it works

When you mount a directory to `/liquibase/changelog`, the container automatically:

1. Detects the presence of the mounted changelog directory
2. Changes the working directory to `/liquibase/changelog`
3. Executes Liquibase commands from that location

This ensures that relative paths in your commands work consistently whether you're using CLI locally or Docker containers in CI/CD pipelines. In most cases, this automatic behavior works seamlessly without any manual intervention.

### 🔍 Search Path Configuration

Liquibase Docker images automatically manage the search path to help locate changelog files and dependencies. The search path is configured with the following priority (highest to lowest):

1. **User-provided `--search-path` CLI argument** (highest priority)
2. **User-provided `LIQUIBASE_SEARCH_PATH` environment variable**
3. **Automatic search path injection** (lowest priority)

#### Understanding Search Path Behavior

When you mount a changelog directory to `/liquibase/changelog`:

- **With relative paths** (`--changelogFile=mychangelog.xml`): The container automatically sets `--search-path=.` to search the current directory (working directory).
- **Without mount or with absolute paths**: The container sets `--search-path=/liquibase/changelog` to help locate files in the default location.

#### Custom Search Paths

If you need to use a custom search path (for example, to include S3 buckets or remote storage locations), the container respects your configuration and **will not override** user-provided search paths:

**Example 1: Using environment variable with multiple search paths**

```bash
docker run --rm \
  --env LIQUIBASE_SEARCH_PATH="/liquibase/changelog,s3://my-bucket/snapshots/" \
  -v /path/to/changelog:/liquibase/changelog \
  liquibase/liquibase --changelogFile=mychangelog.xml update
```

**Example 2: Using CLI argument**

```bash
docker run --rm \
  -v /path/to/changelog:/liquibase/changelog \
  liquibase/liquibase \
  --changelogFile=mychangelog.xml \
  --search-path=/custom/path \
  update
```

**Example 3: Combining relative paths with custom search paths (Correct approach)**

```bash
docker run --rm \
  --env LIQUIBASE_SEARCH_PATH="/liquibase/changelog,/liquibase/shared-changesets" \
  -v /path/to/changelog:/liquibase/changelog \
  -v /path/to/shared:/liquibase/shared-changesets \
  liquibase/liquibase --changelogFile=main.xml update
```

In this example:
- The relative path `main.xml` is found in the working directory (`/liquibase/changelog`)
- Included files are searched **only** in the paths specified by `LIQUIBASE_SEARCH_PATH` (`/liquibase/shared-changesets`). The current directory (`.`) is **not** automatically included. If you want to search both the current directory and a custom path, include both in your configuration: `LIQUIBASE_SEARCH_PATH="/liquibase/changelog,/liquibase/shared-changesets"`

#### Troubleshooting Search Path Issues

If you're experiencing file-not-found errors with custom search paths:

1. **Verify the environment variable is set correctly**: Check that `LIQUIBASE_SEARCH_PATH` is properly formatted (comma-separated for multiple paths)
2. **Check path permissions**: Ensure the Docker container can access mounted directories
3. **Use absolute paths**: For clarity, use absolute paths in your search path configuration
4. **Review Liquibase logs**: Liquibase will output which search path it's using during execution

#### Important: Search Path Behavior with Custom Paths

When you set `LIQUIBASE_SEARCH_PATH` to a custom value:

- **Only the paths you specify are searched** for included files
- The current directory (`.`) is **not automatically added**
- If you want to search multiple locations, **include all of them** in your `LIQUIBASE_SEARCH_PATH` configuration

**Example:** If you want to search both `/liquibase/changelog` and `/liquibase/shared-changesets`:

```bash
# ✓ CORRECT: Include both paths
--env LIQUIBASE_SEARCH_PATH="/liquibase/changelog,/liquibase/shared-changesets"

# ✗ INCORRECT: Only includes shared-changesets, NOT the current directory
--env LIQUIBASE_SEARCH_PATH="/liquibase/shared-changesets"
```

### ⚙️ Using a Configuration File

To use a default configuration file, mount it in your changelog volume and reference it with the `--defaultsFile` argument.

#### Example

```shell
docker run --rm -v /path/to/changelog:/liquibase/changelog liquibase/liquibase --defaultsFile=liquibase.properties update
```

### 📚 Including Drivers and Extensions

Mount a local directory containing additional jars to `/liquibase/lib`.

#### Example

```shell
docker run --rm -v /path/to/changelog:/liquibase/changelog -v /path/to/lib:/liquibase/lib liquibase/liquibase update
```

### 🔍 MySQL Users

Due to licensing restrictions, the MySQL driver is not included. Add it either by extending the image or during runtime via an environment variable.

#### Extending the Image

Dockerfile:

```dockerfile
FROM liquibase:latest

RUN lpm add mysql --global
```

Build:

```shell
docker build . -t liquibase-mysql
```

#### Runtime

```shell
docker run -e INSTALL_MYSQL=true liquibase/liquibase update
```

## 🛠️ Complete Example

Here is a complete example using environment variables and a properties file:

### Environment Variables Example

```shell
docker run --env LIQUIBASE_COMMAND_USERNAME --env LIQUIBASE_COMMAND_PASSWORD --env LIQUIBASE_COMMAND_URL --env LIQUIBASE_PRO_LICENSE_KEY --env LIQUIBASE_COMMAND_CHANGELOG_FILE --rm -v /path/to/changelog:/liquibase/changelog liquibase/liquibase --log-level=info update
```

### Properties File Example

`liquibase.docker.properties` file:

```properties
searchPath: /liquibase/changelog
url: jdbc:postgresql://<IP OR HOSTNAME>:5432/<DATABASE>?currentSchema=<SCHEMA NAME>
changeLogFile: changelog.xml
username: <USERNAME>
password: <PASSWORD>
liquibaseSecureLicenseKey=<PASTE LB Secure LICENSE KEY HERE>
```

CLI:

```shell
docker run --rm -v /path/to/changelog:/liquibase/changelog liquibase/liquibase --defaultsFile=liquibase.docker.properties update
```

## 🔗 Example JDBC URLs

- MS SQL Server: `jdbc:sqlserver://<IP OR HOSTNAME>:1433;database=<DATABASE>`
- PostgreSQL: `jdbc:postgresql://<IP OR HOSTNAME>:5432/<DATABASE>?currentSchema=<SCHEMA NAME>`
- MySQL: `jdbc:mysql://<IP OR HOSTNAME>:3306/<DATABASE>`
- MariaDB: `jdbc:mariadb://<IP OR HOSTNAME>:3306/<DATABASE>`
- DB2: `jdbc:db2://<IP OR HOSTNAME>:50000/<DATABASE>`
- Snowflake: `jdbc:snowflake://<IP OR HOSTNAME>/?db=<DATABASE>&schema=<SCHEMA NAME>`
- Sybase: `jdbc:jtds:sybase://<IP OR HOSTNAME>:/<DATABASE>`
- SQLite: `jdbc:sqlite:/tmp/<DB FILE NAME>.db`

For more details, visit our [Liquibase Documentation](https://docs.liquibase.com/).

<img referrerpolicy="no-referrer-when-downgrade" src="https://static.scarf.sh/a.png?x-pxid=fc4516b5-fc01-40ce-849b-f97dd7be2a34" />

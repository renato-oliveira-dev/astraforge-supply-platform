# Docker Architecture

## Document Information

| Field | Value |
|---|---|
| Project | AstraForge Supply Platform |
| Document | Docker Architecture |
| Status | Draft |
| Version | 0.1.0 |
| Author | Renato Oliveira |

---

# 1. Purpose

This document defines the containerization strategy adopted by the AstraForge Supply Platform.

It establishes:

- container standards
- image construction
- image versioning
- multi-stage builds
- runtime configuration
- health checks
- security hardening
- resource management
- operational recommendations

Every service is packaged and deployed as an OCI-compatible container.

---

# 2. Goals

The container platform must provide:

- portability
- reproducibility
- immutable deployments
- fast startup
- security
- operational consistency

Containers should be identical across all environments.

---

# 3. Container Lifecycle

```
Source Code

↓

Build

↓

Docker Image

↓

Registry

↓

Deployment

↓

Running Container
```

Images are immutable artifacts.

---

# 4. Image Structure

Every image contains only:

- application binaries
- runtime dependencies
- startup scripts
- health endpoints

Development tools are excluded.

---

# 5. Base Images

Preferred base images

- Eclipse Temurin JRE
- Distroless Java
- Chainguard Java

Avoid general-purpose operating system images whenever possible.

---

# 6. Multi-Stage Builds

Every image uses multi-stage builds.

Example

```
Builder Stage

↓

Compile

↓

Test

↓

Package

↓

Runtime Stage
```

Compilation tools are not included in the final image.

---

# 7. Runtime Image

The runtime image should contain:

- JVM
- application
- required certificates
- timezone configuration

Nothing else.

---

# 8. Image Size

Recommendations

- remove build artifacts
- remove package managers
- avoid unnecessary libraries
- minimize operating system footprint

Smaller images improve deployment speed and reduce attack surface.

---

# 9. Image Versioning

Image tags follow semantic versioning.

Examples

```
1.0.0

1.2.5

2.0.0
```

Every image should also include the Git commit SHA as metadata.

Avoid mutable tags such as `latest` in production deployments.

---

# 10. Image Metadata

Recommended OCI labels

- project
- version
- commit
- build date
- vendor
- source repository

This improves traceability.

---

# 11. Configuration

Applications receive configuration through:

- environment variables
- mounted configuration files
- secret providers

Configuration is externalized.

---

# 12. Secrets

Secrets are injected at runtime.

Never bake secrets into container images.

Examples

- database passwords
- OAuth client secrets
- API keys
- certificates

---

# 13. File System

Containers should use a read-only root filesystem whenever feasible.

Writable locations should be explicitly mounted.

Examples

```
/tmp

/logs
```

---

# 14. Non-Root Execution

Containers must run as non-root users.

Example

```
UID 10001
```

Root execution is prohibited unless technically required and documented.

---

# 15. Health Checks

Expose endpoints

```
/actuator/health

/actuator/health/liveness

/actuator/health/readiness
```

Container health should reflect application state.

---

# 16. Resource Limits

Every deployment defines:

- CPU requests
- CPU limits
- memory requests
- memory limits

Containers should never rely on unlimited resources.

---

# 17. JVM Configuration

Recommended settings

- container-aware memory
- G1GC
- UTF-8 encoding
- timezone configuration
- explicit heap sizing when required

JVM parameters should be configurable.

---

# 18. Logging

Applications write logs to:

```
STDOUT

STDERR
```

Containers do not manage log files directly.

Log aggregation is handled by the platform.

---

# 19. Networking

Containers communicate over internal service networks.

Ports should be explicitly exposed.

Avoid unnecessary open ports.

---

# 20. Image Registry

Images are stored in a trusted registry.

Examples

- Amazon ECR
- Azure Container Registry
- Google Artifact Registry
- Harbor

Only signed and approved images should be deployed.

---

# 21. Image Security

Container images must be scanned before publication.

Recommended tools

- Trivy
- Grype
- Docker Scout

Critical vulnerabilities block release pipelines unless explicitly approved.

---

# 22. Supply Chain Security

Recommended practices

- SBOM generation
- image signing
- provenance metadata
- reproducible builds

Supply chain integrity is part of the deployment process.

---

# 23. Startup

Containers should start quickly.

Recommended target

```
< 30 seconds
```

Startup time should include dependency initialization.

---

# 24. Shutdown

Applications must support graceful shutdown.

Flow

```
SIGTERM

↓

Stop Accepting Requests

↓

Finish Active Requests

↓

Close Resources

↓

Exit
```

Abrupt termination should be avoided.

---

# 25. Temporary Storage

Ephemeral storage should be treated as disposable.

Persistent business data must never be stored inside containers.

---

# 26. Testing

Container validation includes:

- startup
- shutdown
- health checks
- configuration loading
- non-root execution
- vulnerability scanning
- image reproducibility

---

# 27. Architecture Rules

Containers:

- are immutable
- are stateless
- externalize configuration
- execute as non-root
- expose health endpoints
- write logs to STDOUT
- support graceful shutdown

---

# 28. Decision Summary

The platform adopts:

- OCI-compatible containers
- multi-stage builds
- immutable images
- semantic versioning
- non-root execution
- read-only filesystem where applicable
- externalized configuration
- container security scanning
- graceful lifecycle management

---

# 29. Next Documentation Step

Next document

```
docs/infrastructure/kubernetes-architecture.md
```

It will define:

- deployment strategy
- namespaces
- autoscaling
- rolling updates
- service discovery
- ingress
- ConfigMaps
- Secrets
- PodDisruptionBudgets
- production topology

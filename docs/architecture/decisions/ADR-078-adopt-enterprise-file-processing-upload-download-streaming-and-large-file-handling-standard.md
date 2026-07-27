# ADR-078: Adopt Enterprise File Processing, Upload, Download, Streaming and Large File Handling Standard

## Document Information

| Field | Value |
|---|---|
| ADR | ADR-078 |
| Title | Adopt Enterprise File Processing, Upload, Download, Streaming and Large File Handling Standard |
| Status | Accepted |
| Date | 2026-07-26 |
| Decision Owners | Enterprise Order Platform Architecture Team |
| Technical Area | File Processing, Upload, Download, Streaming, Object Storage |
| Related Work Items | S3, Spring Boot, Batch Processing, Security, Imports, Exports |
| Supersedes | None |
| Superseded By | None |

---

# 1. Context

Enterprise platforms frequently process files.

Typical scenarios include:

```text
CSV IMPORT

XLSX IMPORT

PDF DOWNLOAD

ZIP PACKAGE

IMAGE UPLOAD

REPORT EXPORT

BANK FILE

LEGACY FIXED-WIDTH FILE

BULK DATA EXPORT
```

A naive implementation may use:

```text
HTTP REQUEST
    |
    v
READ ENTIRE FILE
INTO BYTE[]
    |
    v
PROCESS EVERYTHING
IN MEMORY
    |
    v
SAVE RESULT
```

This works for small files but becomes unsafe as file size and concurrency grow.

Large-file handling introduces concerns around:

```text
Memory

Disk

Network

Security

Malware

File Type

File Names

Compression

Streaming

Object Storage

Data Retention

Partial Processing

Retry

Idempotency
```

---

# 2. Problem Statement

The organization requires standards covering:

- file upload
- file download
- multipart
- object storage
- AWS S3
- presigned URLs
- streaming
- size limits
- MIME validation
- extension validation
- magic bytes
- checksums
- duplicate files
- temporary files
- filename sanitization
- path traversal
- malware scanning
- ZIP bombs
- archive extraction
- CSV
- XLSX
- fixed-width files
- async import
- async export
- chunk processing
- file lifecycle
- retention
- authorization
- encryption
- observability
- cleanup
- retry
- idempotency

---

# 3. Decision Drivers

Primary drivers are:

1. memory safety
2. security
3. scalability
4. data integrity
5. recoverability
6. large-file support
7. low latency
8. bounded resource usage
9. auditability
10. operational visibility
11. retention control
12. cloud-native storage

---

# 4. Decision

Large files MUST NOT be treated as ordinary in-memory request payloads.

The preferred architecture is:

```text
CLIENT
  |
  v
UPLOAD
  |
  v
OBJECT STORAGE
  |
  v
VALIDATE / SCAN
  |
  v
PROCESS ASYNC
  |
  v
RESULT / STATUS
```

For downloads:

```text
CLIENT
  |
  v
AUTHORIZED REQUEST
  |
  v
STREAM / PRESIGNED URL
  |
  v
OBJECT STORAGE
```

---

# 5. Fundamental Principle

```text
Files are streams
and durable artifacts.

They are not giant
byte arrays.

Bound:

size,
memory,
disk,
processing time,
and retention.
```

---

# 6. File Classification

Files SHOULD be classified according to purpose.

Common categories:

```text
USER UPLOAD

SYSTEM IMPORT

REPORT EXPORT

TEMPORARY PROCESSING FILE

ARCHIVE

AUDIT / LEGAL ARTIFACT
```

---

# 7. File Lifecycle

Every material file workflow SHOULD define explicit lifecycle states.

Example:

```text
RECEIVED

UPLOADED

VALIDATING

SCANNING

READY

PROCESSING

PROCESSED

FAILED

EXPIRED

DELETED
```

---

# 8. Hidden File State

File-processing status MUST NOT exist only in application logs.

---

# 9. Upload Size

Every upload endpoint MUST define a maximum file size.

---

# 10. Unlimited Upload

Unlimited file uploads are prohibited.

---

# 11. Request Body Limit

Limits SHOULD be enforced as early as practical.

Potential layers include:

```text
API Gateway

Ingress

Web Server

Application
```

---

# 12. Defense in Depth

Application validation SHOULD remain even when the edge already enforces upload limits.

---

# 13. Different Limits

Different file use cases MAY have different maximum sizes.

Example:

```text
CSV Import = 50 MB

PDF Attachment = 10 MB

Large Data Import = 5 GB
```

according to actual business requirements.

---

# 14. Large Upload

Files above normal application request limits SHOULD prefer direct object-storage upload.

---

# 15. Multipart Upload

HTTP multipart upload MAY be used for reasonably bounded files.

---

# 16. Multipart Memory

Multipart configuration MUST avoid buffering large files entirely into JVM heap.

---

# 17. Disk Spooling

Framework/server multipart temporary disk usage MUST be bounded and monitored.

---

# 18. Temporary Directory

Temporary upload locations MUST have:

```text
Restricted Permissions

Bounded Capacity

Cleanup

Monitoring
```

---

# 19. Local Disk

Container-local disk SHOULD NOT be treated as durable file storage.

---

# 20. Pod Replacement

Files stored only inside a pod may disappear during:

```text
Restart

Rescheduling

Scaling

Node Failure
```

---

# 21. Object Storage

Durable application files SHOULD use object storage such as AWS S3 where appropriate.

---

# 22. Object Storage Benefits

Object storage provides:

```text
Durability

Scalability

Lifecycle Policies

Encryption

Direct Upload/Download

Versioning Options
```

---

# 23. Database BLOB

Large binary files SHOULD NOT normally be stored directly in transactional database rows unless the transactional semantics specifically justify it.

---

# 24. Database Metadata

The database MAY store file metadata such as:

```text
fileId

objectKey

contentType

size

checksum

status

createdAt

owner
```

---

# 25. Object Key

Object keys MUST be system-generated or safely controlled.

---

# 26. User Filename as Key

Raw user filenames MUST NOT directly determine storage paths.

---

# 27. Example

Avoid:

```text
uploads/{userFilename}
```

Prefer:

```text
uploads/{fileId}/source
```

while storing original filename as metadata.

---

# 28. Original Filename

Original filenames MAY be preserved for display.

They MUST be treated as untrusted input.

---

# 29. Filename Sanitization

Filenames MUST prevent:

```text
Path Traversal

Control Characters

Invalid Separators

Excessive Length

Header Injection
```

---

# 30. Path Traversal

Names such as:

```text
../../etc/passwd
```

MUST NOT influence filesystem/object path resolution.

---

# 31. Windows Path Traversal

Paths such as:

```text
..\..\file
```

must also be handled.

---

# 32. Absolute Path

Absolute filenames MUST NOT be trusted.

---

# 33. Content-Disposition

Download filenames used in HTTP headers MUST be safely encoded.

---

# 34. Filename Length

Filename length MUST be bounded.

---

# 35. File Extension

Extension validation alone is insufficient.

---

# 36. MIME Type

Client-provided MIME type MUST NOT be blindly trusted.

---

# 37. Content Type Validation

Validation SHOULD consider applicable:

```text
Extension

Declared MIME Type

Detected File Signature

Actual Parser Compatibility
```

---

# 38. Magic Bytes

Binary formats SHOULD use file-signature detection where practical.

---

# 39. Text File

Text formats such as CSV require parser/content-level validation rather than magic-byte detection alone.

---

# 40. File Type Allowlist

Upload endpoints SHOULD use file-type allowlists.

---

# 41. File Type Denylist

A denylist alone is insufficient.

---

# 42. Arbitrary Executable

Executable files SHOULD NOT be accepted unless explicitly required.

---

# 43. Double Extension

Names such as:

```text
report.pdf.exe
```

MUST NOT bypass type policy.

---

# 44. Content Validation

Successfully identifying a file format does not prove its contents are valid for the business process.

---

# 45. Malware Scanning

Untrusted uploads SHOULD be malware-scanned when threat/risk requires it.

---

# 46. Scan Before Processing

Files SHOULD NOT be made available for downstream processing/download before required security scanning succeeds.

---

# 47. Quarantine

New files MAY enter a quarantine area/state until validation completes.

---

# 48. Scan Status

Possible states:

```text
PENDING_SCAN

CLEAN

INFECTED

SCAN_FAILED
```

---

# 49. Scan Failure

Scanner unavailability MUST have explicit semantics.

Possible policies include:

```text
FAIL CLOSED

RETRY LATER
```

---

# 50. Fail Open

Unscanned files SHOULD NOT be released merely because the malware scanner is temporarily unavailable unless risk policy explicitly allows it.

---

# 51. Infected File

Infected files MUST NOT proceed to normal processing.

---

# 52. Infected Retention

Retention of infected samples, if required for security investigation, MUST use restricted quarantine storage.

---

# 53. File Hash

Files SHOULD have a cryptographic checksum when integrity or duplicate detection matters.

---

# 54. SHA-256

SHA-256 is suitable for ordinary integrity fingerprinting.

---

# 55. Weak Hash

MD5/SHA-1 SHOULD NOT be used for security-sensitive integrity guarantees.

---

# 56. Checksum Use

Checksum MAY support:

```text
Integrity Verification

Duplicate Detection

Upload Completion Validation

Audit
```

---

# 57. Hash Is Not Identity

Checksum alone SHOULD NOT automatically define business file identity.

Two identical files may legitimately represent separate business submissions.

---

# 58. Duplicate Detection

Duplicate-file semantics MUST be defined by business rules.

---

# 59. Duplicate Dimensions

Duplicate detection MAY use:

```text
Source System

Business Date

File Type

Business Identifier

Checksum
```

---

# 60. Idempotent Import

Reprocessing the same logical file SHOULD not produce unintended duplicate business effects.

---

# 61. File ID

Every accepted file SHOULD receive a stable internal identifier.

---

# 62. File Metadata

Recommended metadata includes:

```text
fileId

originalFilename

contentType

size

checksum

source

status

createdAt

processingStartedAt

processingFinishedAt

errorCode

objectKey
```

---

# 63. Metadata Privacy

File metadata MUST follow privacy standards.

---

# 64. S3 Key Privacy

Personal data SHOULD not unnecessarily appear in S3 object keys.

---

# 65. Direct Upload

Large files SHOULD use direct-to-object-storage upload where practical.

---

# 66. Presigned Upload

Canonical pattern:

```text
CLIENT
   |
   v
REQUEST UPLOAD AUTHORIZATION
   |
   v
APPLICATION CREATES
UPLOAD INTENT
   |
   v
PRESIGNED URL
   |
   v
CLIENT -> S3
```

---

# 67. Upload Intent

The application SHOULD create an upload record before issuing a presigned URL for business-controlled files.

---

# 68. Presigned URL Lifetime

Presigned URLs MUST have short bounded expiration.

---

# 69. Presigned URL Scope

A presigned URL MUST permit only the intended operation on the intended object.

---

# 70. Wildcard Presigned Access

Presigned credentials MUST NOT provide broad bucket access.

---

# 71. Upload Completion

Direct upload completion SHOULD be verified before processing.

---

# 72. Completion Validation

Validate:

```text
Object Exists

Expected Size

Checksum where available

Metadata

Authorization Context
```

---

# 73. Multipart S3 Upload

Very large S3 uploads SHOULD use multipart upload.

---

# 74. Multipart Benefits

Multipart upload provides:

```text
Chunked Transfer

Parallel Part Upload

Resume Capability

Large File Support
```

---

# 75. Multipart Abort

Incomplete multipart uploads MUST eventually be aborted/cleaned up.

---

# 76. Lifecycle Rule

Object-storage lifecycle policies SHOULD clean abandoned uploads.

---

# 77. Download

Downloads MUST enforce authorization.

---

# 78. Guessable URL

Knowledge of a file ID/object key MUST NOT by itself grant file access.

---

# 79. Download Through App

Small or access-sensitive files MAY stream through the application.

---

# 80. Large Download

Large files SHOULD generally use object-storage direct download/presigned URL after authorization.

---

# 81. Presigned Download

Canonical flow:

```text
CLIENT
   |
   v
AUTHORIZED REQUEST
   |
   v
APPLICATION
   |
   v
SHORT-LIVED
PRESIGNED URL
   |
   v
OBJECT STORAGE
```

---

# 82. Download Expiration

Presigned download links MUST have short expiration appropriate to use case.

---

# 83. Cache Headers

Sensitive downloads MUST use appropriate cache-control headers.

---

# 84. Browser Cache

Highly sensitive files SHOULD prevent inappropriate shared/browser caching where required.

---

# 85. Range Requests

Large downloads MAY support HTTP range requests where object storage/proxy supports them.

---

# 86. Streaming

Application-mediated downloads MUST use streaming.

---

# 87. `byte[]`

Large files MUST NOT be loaded fully into:

```java
byte[]
```

before transmission.

---

# 88. Base64

Large binary files SHOULD NOT be embedded as Base64 inside JSON APIs.

---

# 89. Base64 Overhead

Base64 increases payload size and memory pressure.

---

# 90. Binary Contract

Binary transfer SHOULD use an appropriate binary/file endpoint.

---

# 91. Streaming Backpressure

Streaming implementations MUST account for slow consumers.

---

# 92. Slow Client

A slow client MUST NOT cause uncontrolled memory buffering.

---

# 93. Response Buffer

Application/proxy buffering MUST remain bounded.

---

# 94. Streaming Connection

Long downloads consume connections and MUST be included in capacity planning.

---

# 95. Timeout

Download/upload timeouts SHOULD differ from ordinary short JSON request timeouts where justified.

---

# 96. Infinite Transfer

File transfer timeouts MUST still be bounded.

---

# 97. Async Import

Large imports SHOULD be asynchronous.

---

# 98. Import Flow

Preferred:

```text
UPLOAD
   |
   v
202 / FILE ID
   |
   v
VALIDATE
   |
   v
PROCESS
   |
   v
STATUS
```

---

# 99. Sync Import

Synchronous processing is acceptable only for small bounded files with predictable processing duration.

---

# 100. Import Status

Clients SHOULD be able to obtain import status for long-running operations.

Example:

```text
GET /imports/{fileId}
```

---

# 101. Import States

Recommended:

```text
RECEIVED

VALIDATING

PROCESSING

COMPLETED

PARTIALLY_COMPLETED

FAILED
```

---

# 102. Validation Stage

Structural validation SHOULD occur before expensive business processing where practical.

---

# 103. File Header Validation

Imports SHOULD validate:

```text
Expected Columns

Column Count

Required Headers

Layout Version

Encoding
```

before processing records.

---

# 104. Layout Version

Long-lived integration files SHOULD include or otherwise define a versioned layout contract.

---

# 105. Fixed-Width File

Fixed-width files MUST validate:

```text
Record Length

Record Type

Field Positions

Encoding

Trailer/Control Totals
```

---

# 106. CSV

CSV processing MUST explicitly define:

```text
Delimiter

Quote Character

Escape Rules

Encoding

Header

Line Ending
```

---

# 107. CSV Is Not Split

CSV MUST NOT be parsed using naive:

```java
line.split(",")
```

when quoted delimiters and escaping are allowed.

---

# 108. CSV Injection

Exports intended for spreadsheet applications MUST consider formula injection.

---

# 109. Formula Injection

Values beginning with:

```text
=

+

-

@
```

may be interpreted as formulas by spreadsheet software.

---

# 110. Contextual Protection

Spreadsheet export sanitization MUST be specific to spreadsheet output and MUST NOT mutate source/domain data.

---

# 111. XLSX

Large XLSX processing SHOULD use streaming/event-based APIs when possible.

---

# 112. Whole Workbook

Loading a huge workbook entirely into memory SHOULD be avoided.

---

# 113. Shared Strings

XLSX parsers can consume substantial memory because of shared strings/styles and MUST be load-tested with realistic files.

---

# 114. Formula Evaluation

Untrusted spreadsheet formulas SHOULD NOT be blindly evaluated.

---

# 115. Macro-Enabled File

Macro-enabled spreadsheet formats SHOULD be rejected unless explicitly required.

---

# 116. Archive Upload

ZIP/archive uploads require additional controls.

---

# 117. ZIP Bomb

Archive processing MUST defend against decompression bombs.

---

# 118. Expansion Limits

Archive extraction MUST define limits such as:

```text
Maximum File Count

Maximum Total Expanded Size

Maximum Per-Entry Size

Maximum Compression Ratio

Maximum Nesting
```

---

# 119. Nested Archive

Nested archives SHOULD be rejected or strictly bounded unless explicitly required.

---

# 120. Archive Path Traversal

Archive entry names MUST prevent Zip Slip/path traversal.

---

# 121. Example

An entry:

```text
../../application.yml
```

MUST NOT escape the intended extraction directory.

---

# 122. Symlink

Archive extraction SHOULD reject unsafe symbolic links where applicable.

---

# 123. Extraction Directory

Archive extraction MUST occur in controlled isolated temporary storage.

---

# 124. Archive Cleanup

Temporary extracted content MUST be deleted after bounded lifecycle completion.

---

# 125. File Parser Security

Parsing libraries MUST follow dependency/supply-chain governance.

---

# 126. Parser Vulnerability

File parsing libraries are high-risk because malformed input can exploit parser defects.

---

# 127. Parser Limits

Parser-level resource limits SHOULD be enabled where supported.

---

# 128. XML Files

If XML files are accepted, XXE/external-entity processing MUST be disabled unless explicitly required and secured.

---

# 129. PDF

PDF uploads SHOULD be treated as untrusted complex binary input.

---

# 130. Image Files

Image processing libraries MUST enforce size/dimension/resource limits.

---

# 131. Image Bomb

A compressed image can expand to enormous memory requirements.

Pixel dimensions MUST be bounded.

---

# 132. Record Processing

Large imports MUST process records incrementally.

---

# 133. File Chunk

Conceptually:

```text
FILE
 |
 +--> RECORD 1..1000
 |       |
 |       v
 |     COMMIT
 |
 +--> RECORD 1001..2000
         |
         v
       COMMIT
```

where semantics permit.

---

# 134. One Transaction Per File

A single transaction covering a very large import SHOULD be avoided.

---

# 135. Partial Success

File imports MUST define whether processing is:

```text
ALL OR NOTHING

PARTIAL SUCCESS

RECORD-LEVEL REJECTION
```

---

# 136. Atomic Import

All-or-nothing semantics SHOULD only be used when business rules genuinely require them and file size makes transaction cost acceptable.

---

# 137. Reject Records

For partial processing, rejected records SHOULD be represented explicitly.

---

# 138. Reject File

Rejected-record output SHOULD be safe, bounded and authorized.

---

# 139. Error Line

Import errors SHOULD identify the affected logical record/line without exposing unnecessary sensitive data.

---

# 140. Error Count

Very large invalid files MUST NOT produce unbounded in-memory error collections.

---

# 141. Max Errors

Validation SHOULD cap collected error details.

Example:

```text
10,000 errors found

first 100 returned/reported
```

---

# 142. Error Artifact

Large validation reports SHOULD be stored as a separate downloadable artifact.

---

# 143. Retry

Import retries MUST be idempotent.

---

# 144. Whole File Retry

Retrying a complete large import may be expensive.

Checkpoint/restart SHOULD be considered.

---

# 145. Checkpoint

Long-running import jobs SHOULD persist progress.

---

# 146. Line Number Checkpoint

Line number alone may be insufficient if the source file can change.

---

# 147. Immutable Input

Processing MUST use an immutable stored source object.

---

# 148. File Version

The exact object/version/checksum being processed SHOULD be known.

---

# 149. Reprocessing

Reprocessing SHOULD target the immutable original file or explicitly versioned replacement.

---

# 150. Concurrent Processing

The same logical file MUST NOT be processed concurrently unless explicitly partitioned.

---

# 151. File Claim

Processing SHOULD atomically claim the file.

---

# 152. Stale Claim

Claims MUST be recoverable if a worker dies.

---

# 153. Lease

Lease-based claims MAY support recovery.

---

# 154. File Lock

Filesystem locks alone SHOULD NOT coordinate distributed Kubernetes workers.

---

# 155. Parallel File Processing

Different independent files MAY be processed in parallel.

---

# 156. Concurrency Limit

File-processing concurrency MUST be bounded.

---

# 157. Virtual Threads

Virtual Threads MAY support I/O-heavy file orchestration but MUST NOT remove downstream/object-storage concurrency limits.

---

# 158. CPU Processing

Compression, parsing, encryption and checksum computation may be CPU-intensive and MUST be included in capacity planning.

---

# 159. Download Export

Large exports SHOULD follow ADR-066 asynchronous-export architecture.

---

# 160. Async Export Flow

```text
POST /exports
      |
      v
202 ACCEPTED
      |
      v
BACKGROUND GENERATION
      |
      v
OBJECT STORAGE
      |
      v
AUTHORIZED DOWNLOAD
```

---

# 161. Export Snapshot

The export MUST define what data consistency point it represents.

---

# 162. Pagination During Export

Export traversal MUST remain stable as source data changes.

---

# 163. Snapshot Strategies

Possible strategies include:

```text
Business Cutoff

Stable Keyset Traversal

Database Snapshot

Explicit Created-At Boundary
```

depending on requirements.

---

# 164. Export Memory

Export generation MUST stream/chunk output rather than accumulating all rows in memory.

---

# 165. CSV Export

CSV export SHOULD write incrementally.

---

# 166. XLSX Export

Large XLSX export SHOULD use streaming workbook APIs.

---

# 167. Export Row Limit

Interactive spreadsheet exports MAY need explicit row limits.

---

# 168. XLSX Limit

Spreadsheet format row limits and practical workbook sizes MUST be considered.

---

# 169. Massive Export

Extremely large datasets SHOULD use scalable formats/workflows rather than one giant XLSX file.

---

# 170. Compression

Exports MAY be compressed where beneficial.

---

# 171. Compression Cost

Compression adds CPU and may increase latency.

---

# 172. ZIP Export

ZIP creation SHOULD be streaming where possible.

---

# 173. Password-Protected ZIP

Password-protected archives SHOULD NOT be invented as an ad hoc substitute for proper authorization/encryption architecture.

---

# 174. Encryption at Rest

Object storage containing confidential files MUST use approved encryption at rest.

---

# 175. Encryption in Transit

Upload/download traffic MUST use TLS.

---

# 176. Object Ownership

File ownership/access metadata MUST remain explicit.

---

# 177. Cross-Tenant Access

One customer/tenant/user MUST NOT access another's file because object keys are guessable.

---

# 178. S3 Public Access

Sensitive buckets MUST block public access.

---

# 179. Bucket Policy

Bucket policy MUST enforce intended service identities and access patterns.

---

# 180. IAM

Workload IAM permissions SHOULD follow least privilege.

---

# 181. Delete Permission

A service SHOULD only receive delete permissions when its responsibility requires them.

---

# 182. Object Versioning

S3 versioning MAY be enabled where recovery/audit requirements justify it.

---

# 183. Versioning and Deletion

Object versioning affects privacy deletion/retention because old versions may remain recoverable.

---

# 184. Retention

Every file category MUST have an explicit retention rule.

---

# 185. Temporary File Retention

Temporary uploads and intermediate artifacts SHOULD have short retention.

---

# 186. Failed Upload

Abandoned/failed uploads MUST eventually be cleaned up.

---

# 187. Processed Source

Retention of processed source files MUST follow business/legal needs.

---

# 188. Export Retention

Generated export files SHOULD expire automatically.

---

# 189. Lifecycle Policy

Object-store lifecycle rules SHOULD enforce retention where appropriate.

---

# 190. Delete Workflow

Database metadata and object deletion MUST remain consistent enough to avoid orphaned sensitive artifacts.

---

# 191. Orphan Object

Cleanup SHOULD detect uploaded objects with no valid metadata/workflow record.

---

# 192. Orphan Metadata

Cleanup SHOULD detect metadata referencing missing objects where meaningful.

---

# 193. Deletion Retry

Object deletion should be retryable and idempotent.

---

# 194. Backup

File/object backup and replication MUST follow retention/privacy requirements.

---

# 195. Legal Hold

Files subject to legal hold MUST not be removed by ordinary lifecycle policies.

---

# 196. Audit

Critical file operations SHOULD be auditable.

---

# 197. Audit Events

Applicable operations include:

```text
UPLOAD AUTHORIZED

UPLOAD COMPLETED

FILE PROCESSED

DOWNLOAD AUTHORIZED

FILE DELETED

EXPORT GENERATED
```

---

# 198. Audit Payload

Audit SHOULD reference file identity and action rather than copying complete file content.

---

# 199. Observability

File workflows MUST be observable.

---

# 200. Metrics

Useful metrics include:

```text
uploads_started

uploads_completed

uploads_failed

bytes_uploaded

files_scanned

scan_failures

processing_duration

records_processed

records_rejected

downloads

exports_generated
```

---

# 201. Cardinality

`fileId` MUST NOT be used as a metric label.

---

# 202. File Type Metric

A bounded logical file type MAY be a metric dimension.

---

# 203. Original Filename Metric

Original filenames MUST NOT be metric labels.

---

# 204. Logs

Logs SHOULD include safe:

```text
fileId

workflow

status

size

logical type

duration
```

---

# 205. Log Full Filename

Original filenames MAY contain personal/sensitive data and SHOULD be logged only when necessary.

---

# 206. File Content Logging

File contents MUST NOT be logged indiscriminately.

---

# 207. CSV Line Logging

Invalid lines containing personal data MUST NOT automatically be dumped to logs.

---

# 208. Trace

Tracing SHOULD focus on file workflow and external calls rather than creating one span per row.

---

# 209. Alerting

Alerts SHOULD include applicable:

```text
Upload Failure Surge

Malware Scan Failure

Processing Backlog

Stuck File

Object Storage Failure

Export Backlog

Cleanup Failure

Disk Spool Saturation
```

---

# 210. Backlog

Queue age of unprocessed files SHOULD be monitored.

---

# 211. Stuck File

A file remaining in:

```text
PROCESSING
```

beyond expected duration MUST become visible.

---

# 212. Storage Capacity

Temporary local disk utilization MUST be monitored if files are spooled locally.

---

# 213. Testing Strategy

File processing requires dedicated security and boundary tests.

---

# 214. Size Test

Test:

```text
MAX_SIZE - 1

MAX_SIZE

MAX_SIZE + 1
```

---

# 215. File Type Test

Verify unsupported types are rejected.

---

# 216. MIME Spoof Test

Test a file whose extension and content type disagree.

---

# 217. Filename Test

Test:

```text
../

..\

absolute paths

control characters

very long names
```

---

# 218. Checksum Test

Verify checksum mismatch is rejected where integrity validation is required.

---

# 219. Duplicate Test

File duplicate/idempotency semantics MUST have tests.

---

# 220. Malware Test

Malware-scanning integration SHOULD test:

```text
Clean

Infected

Scanner unavailable

Scanner timeout
```

using safe test fixtures.

---

# 221. ZIP Bomb Test

Archive extraction MUST test resource-limit protections without deploying truly harmful payloads.

---

# 222. Zip Slip Test

Archive entries attempting path traversal MUST be rejected.

---

# 223. CSV Test

CSV tests SHOULD cover:

```text
Quoted delimiter

Escaped quote

Empty field

Encoding

Long row

Malformed row
```

---

# 224. XLSX Test

XLSX parsing SHOULD test malformed and large representative files.

---

# 225. Formula Test

Spreadsheet export SHOULD test formula-injection protection where applicable.

---

# 226. Streaming Test

Large file tests SHOULD verify memory remains bounded.

---

# 227. Import Restart Test

Critical import should:

```text
Fail Midway

Restart

Resume/Retry

Produce No Duplicate Effects
```

---

# 228. Partial Success Test

Where partial success exists, accepted/rejected counts MUST be verified.

---

# 229. Max Error Test

Invalid files with many errors MUST verify error reporting remains bounded.

---

# 230. Presigned URL Test

Tests SHOULD verify:

```text
Expiration

Object Scope

Method

Authorization
```

---

# 231. Cross-User Download Test

Authorization tests MUST verify one principal cannot access another principal's file.

---

# 232. Cleanup Test

Expired temporary/export objects SHOULD be removed according to policy.

---

# 233. Testcontainers

Local object-storage emulation MAY be used where semantically sufficient, while critical cloud behavior SHOULD be validated in representative environments.

---

# 234. File Processing Review Checklist

```text
[ ] What file types are allowed?

[ ] What is the maximum size?

[ ] Can it upload directly to object storage?

[ ] Is multipart required?

[ ] Is client MIME trusted? It should not be.

[ ] Is file signature validated?

[ ] Is malware scanning required?

[ ] What happens if scanning fails?

[ ] Is filename treated as untrusted?

[ ] Could path traversal occur?

[ ] Is checksum required?

[ ] How are duplicates handled?

[ ] Is processing synchronous or asynchronous?

[ ] Is processing memory bounded?

[ ] Is processing restartable?

[ ] Is the same file safe to process twice?

[ ] Is partial success allowed?

[ ] Are errors bounded?

[ ] Can archives be uploaded?

[ ] Are archive expansion limits configured?

[ ] Can a ZIP entry escape the extraction path?

[ ] Are downloads authorized?

[ ] Should download use a presigned URL?

[ ] What is file retention?

[ ] How are abandoned uploads cleaned?

[ ] Are object keys free of unnecessary PII?

[ ] Are sensitive files encrypted?

[ ] Is processing observable?
```

---

# 235. File Security Fitness Functions

Stable controls SHOULD be automated where practical.

Examples:

```text
[ ] Upload size limit configured

[ ] Allowed file types explicitly declared

[ ] Raw filename not used as filesystem path

[ ] Direct object keys generated by application

[ ] Public S3 access disabled

[ ] Sensitive buckets encrypted

[ ] Presigned URLs have expiration

[ ] Archive extraction has path validation

[ ] Archive expansion limits configured

[ ] Large processing uses streaming/chunking

[ ] Temporary files have cleanup policy

[ ] File contents excluded from logs
```

---

# 236. Enterprise File Gate

A file workflow is not considered compliant when applicable conditions include:

```text
[ ] Upload size is unlimited

[ ] Large file is fully loaded into JVM byte[]

[ ] Raw user filename controls storage path

[ ] File type is trusted solely from extension

[ ] File type is trusted solely from client MIME

[ ] Untrusted file bypasses required malware scan

[ ] Archive can extract outside controlled directory

[ ] Archive expansion is unbounded

[ ] File processing loads complete huge dataset into memory

[ ] Same file can create duplicate business effects

[ ] Sensitive S3 bucket is public

[ ] Presigned URL has excessive lifetime or broad scope

[ ] Download authorization relies only on knowing object key

[ ] Temporary files have no cleanup

[ ] Failed uploads remain forever

[ ] File contents can appear in logs

[ ] Large exports are generated synchronously in request memory

[ ] Retention policy is undefined
```

---

# 237. Anti-Patterns

The following are prohibited or strongly discouraged:

- unlimited multipart upload
- large file as `byte[]`
- large binary encoded as Base64 JSON
- pod-local disk as durable storage
- raw filename as object key/path
- extension-only validation
- client MIME trust
- processing unscanned untrusted files where scanning is required
- unrestricted archive extraction
- ZIP bomb vulnerability
- Zip Slip vulnerability
- whole huge XLSX workbook in memory
- naive CSV `split(",")`
- spreadsheet formula injection
- one giant import transaction
- unbounded error collection
- duplicate processing with no idempotency
- public S3 buckets for sensitive files
- permanent presigned URLs
- file content in application logs
- exports retained forever
- abandoned multipart uploads never cleaned
- synchronous generation of massive files

---

# 238. Positive Consequences

The decision provides:

- bounded JVM memory usage
- scalable large-file handling
- safer uploads
- stronger object-storage security
- malware protection
- safer archive processing
- resumable/restartable imports
- controlled export architecture
- explicit file lifecycle
- better retention governance
- improved auditability
- stronger operational diagnostics

---

# 239. Negative Consequences

The decision introduces:

- object-storage infrastructure
- asynchronous processing
- malware scanning
- file lifecycle metadata
- cleanup jobs
- parser security controls
- retention management
- more integration testing

These costs are accepted because untrusted and large files create significant availability and security risks.

---

# 240. Neutral Consequences

The decision also means:

- not every file requires malware scanning
- not every upload must go directly to S3
- not every import requires Spring Batch
- not every duplicate checksum means duplicate business submission
- not every file should be retained after processing
- streaming still consumes finite connections and bandwidth
- presigned URLs simplify transfer but do not replace authorization
- asynchronous processing is preferable for large files but adds status management

---

# 241. Risks and Mitigations

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| Memory exhaustion | Critical | Medium | Streaming/chunking |
| Malware upload | Critical | Medium | Scan/quarantine |
| Path traversal | Critical | Medium | Safe generated paths |
| ZIP bomb | Critical | Low/Medium | Expansion limits |
| Unauthorized download | Critical | Medium | Resource authorization |
| Duplicate import | High | Medium | Idempotency |
| Disk exhaustion | Critical | Medium | Spool limits/cleanup |
| Object leak | Critical | Low/Medium | Private buckets |
| Parser vulnerability | High | Medium | Dependency governance |
| Orphaned sensitive file | High | Medium | Lifecycle cleanup |

---

# 242. Implementation Guidance

The following rules are mandatory:

1. Every upload must have a maximum size.
2. Large files must not be fully buffered into JVM memory.
3. Durable files should use approved object storage.
4. Pod-local storage must not be treated as durable.
5. File metadata should be stored separately from large file content.
6. Raw filenames must not define storage paths.
7. Original filenames must be treated as untrusted.
8. Allowed file types must be explicit.
9. Extension/MIME values must not be trusted independently.
10. Required malware scanning must complete before normal processing.
11. File integrity checksums should be used where required.
12. Duplicate-file semantics must be business-defined.
13. Large uploads/downloads should use direct object-storage transfer where practical.
14. Presigned URLs must be short-lived and narrowly scoped.
15. Downloads must enforce resource authorization.
16. Large imports must run asynchronously.
17. Large imports must process data incrementally.
18. Large imports must have bounded transaction and memory models.
19. Critical imports must be idempotent/restartable.
20. Partial-success semantics must be explicit.
21. Error collection/reporting must be bounded.
22. CSV must use a real parser.
23. Large XLSX files must use streaming/event-oriented processing where practical.
24. Spreadsheet exports must consider formula injection.
25. Archive extraction must defend against Zip Slip and decompression bombs.
26. Archive file count, size, ratio and nesting must be bounded.
27. Temporary files and abandoned uploads must be cleaned automatically.
28. Sensitive files must follow encryption, privacy and retention standards.
29. Object-storage access must follow least privilege.
30. File contents must not be indiscriminately logged.
31. Critical file operations should be auditable.
32. File-processing backlog and stuck workflows must be observable.
33. Large exports must follow asynchronous streaming generation.
34. File security and restart/idempotency behavior must have automated tests.

---

# 243. Validation

This ADR will be validated through:

- Java 21
- Spring Boot
- Spring Multipart
- AWS S3
- S3 multipart upload
- presigned URLs
- AWS IAM
- object-storage encryption
- malware-scanning integration where required
- Apache Commons CSV or approved parser
- Apache POI streaming APIs where applicable
- Spring Batch where applicable
- PostgreSQL metadata
- Testcontainers
- security tests
- large-file tests
- memory profiling
- CI/CD quality gates
- lifecycle cleanup validation

---

# 244. Success Criteria

The decision is successful when:

- large files no longer require proportional JVM heap
- upload sizes are consistently bounded
- unsupported/spoofed formats are rejected
- malicious archives cannot escape extraction boundaries
- archive expansion cannot exhaust infrastructure unchecked
- large imports can recover from interruptions
- duplicate file submissions do not create duplicate business effects
- sensitive files remain private
- direct transfer reduces application-network bottlenecks
- large exports no longer block interactive requests
- temporary and expired artifacts are automatically removed
- file-processing backlog and failures are operationally visible

---

# 245. Alternatives Rejected

## 245.1 Store Every File as Database BLOB

Rejected as the general strategy because large object storage provides better scalability and lifecycle capabilities for most file workloads.

---

## 245.2 Load Entire File into Memory

Rejected because file size and concurrency can exhaust JVM heap.

---

## 245.3 Trust Extension/MIME

Rejected because both are attacker-controlled or easily spoofed.

---

## 245.4 Use User Filename as Storage Path

Rejected because of path traversal, collisions and information leakage.

---

## 245.5 Synchronous Large Import

Rejected because request lifetime, memory and failure-recovery characteristics are unsuitable.

---

## 245.6 Unrestricted ZIP Extraction

Rejected because of decompression bombs and path traversal.

---

## 245.7 Permanent Public Download URL

Rejected because sensitive file access must remain authorized and time bounded.

---

# 246. Related Decisions

This ADR extends and implements:

- ADR-013: Use Testcontainers for Integration Testing
- ADR-034: Java 21 Concurrency and Parallelism Standards
- ADR-037: Application Security and Secure Coding Standards
- ADR-040: Production Reliability and Operational Readiness Standards
- ADR-050: Enterprise Architecture Baseline
- ADR-053: Enterprise Testing Strategy and Quality Engineering Standard
- ADR-054: Enterprise Performance Engineering and Capacity Standard
- ADR-055: Enterprise Resilience Engineering Standard
- ADR-060: Enterprise AWS Cloud, Kubernetes, Container and Runtime Deployment Standard
- ADR-061: Enterprise CI/CD, DevSecOps, Software Supply Chain and Release Engineering Standard
- ADR-062: Enterprise Logging, Observability, OpenTelemetry and Production Diagnostics Standard
- ADR-063: Enterprise Configuration Management, Secrets, Feature Flags and Runtime Parameter Governance Standard
- ADR-066: Enterprise API Performance, Data Retrieval, Pagination, Filtering, Sorting and Bulk Processing Standard
- ADR-067: Enterprise Error Handling, Exception Taxonomy, Problem Details and Failure Contract Standard
- ADR-068: Enterprise Test Architecture, Test Data, Mocking, Testcontainers and Coverage Governance Standard
- ADR-071: Enterprise Data Privacy, PII, Auditability, Retention and Secure Data Handling Standard
- ADR-072: Enterprise Distributed Transactions, Saga, Idempotency, Consistency and Compensation Standard
- ADR-075: Enterprise Application Lifecycle, Health Checks, Readiness, Liveness, Startup and Graceful Shutdown Standard
- ADR-077: Enterprise Scheduled Jobs, Batch Processing, Distributed Scheduling and Workload Coordination Standard

---

# 247. References

- AWS S3 Documentation
- AWS S3 Multipart Upload Documentation
- AWS Presigned URL Documentation
- OWASP File Upload Cheat Sheet
- OWASP Path Traversal Guidance
- OWASP CSV Injection Guidance
- Apache POI Documentation
- Apache Commons CSV Documentation
- Spring Boot Multipart Documentation
- Spring Batch Documentation
- ZIP File Format Specification
- Java NIO Documentation
- NIST Secure Software Development Framework

---

# 248. Review History

| Date | Reviewer | Result | Notes |
|---|---|---|---|
| 2026-07-26 | Enterprise Order Platform Architecture Team | Approved | Initial enterprise secure file-processing and large-file baseline |

---

# 249. Decision Summary

Large upload becomes:

```text
CLIENT
   |
   v
REQUEST UPLOAD
   |
   v
AUTHORIZED FILE ID
   |
   v
PRESIGNED URL
   |
   v
OBJECT STORAGE
   |
   v
VERIFY
   |
   v
SCAN
   |
   v
ASYNC PROCESS
```

rather than:

```text
CLIENT
   |
   v
5 GB HTTP REQUEST
   |
   v
byte[]
   |
   v
OUT OF MEMORY
```

File identity becomes:

```text
fileId
   |
   +--> originalFilename
   +--> objectKey
   +--> checksum
   +--> size
   +--> type
   +--> status
```

rather than using:

```text
../../customer-report.xlsx
```

as a path.

File validation becomes:

```text
FILE
 |
 +--> SIZE
 |
 +--> ALLOWED TYPE
 |
 +--> CONTENT SIGNATURE
 |
 +--> PARSER VALIDATION
 |
 +--> MALWARE SCAN
 |
 v
READY
```

Archive handling becomes:

```text
ARCHIVE
   |
   +--> ENTRY COUNT LIMIT
   +--> ENTRY SIZE LIMIT
   +--> TOTAL SIZE LIMIT
   +--> COMPRESSION RATIO LIMIT
   +--> PATH VALIDATION
   +--> NESTING LIMIT
   |
   v
SAFE EXTRACTION
```

Import processing becomes:

```text
IMMUTABLE SOURCE FILE
        |
        v
CHUNK 1
   |
COMMIT
   |
CHUNK 2
   |
COMMIT
   |
...
   |
STATUS
```

Duplicate protection becomes:

```text
SAME LOGICAL FILE
      |
      v
PROCESS AGAIN?
      |
      v
IDEMPOTENT BUSINESS EFFECT
```

Large export becomes:

```text
CLIENT
 |
 v
POST /exports
 |
 v
202
 |
 v
BACKGROUND GENERATION
 |
 v
STREAM TO OBJECT STORAGE
 |
 v
SHORT-LIVED AUTHORIZED DOWNLOAD
```

Download authorization becomes:

```text
CLIENT
  |
  v
CAN ACCESS fileId?
  |
 +--+--+
 |     |
NO    YES
 |     |
403   PRESIGNED
      DOWNLOAD
```

Retention becomes:

```text
FILE CREATED
    |
    v
PURPOSE
    |
    v
RETENTION RULE
    |
    v
EXPIRE
    |
    v
DELETE
```

The complete file-handling equation is:

```text
BOUNDED FILE SIZE
        +
STREAMING
        +
OBJECT STORAGE
        +
SAFE FILENAMES
        +
TYPE VALIDATION
        +
MALWARE SCANNING
        +
CHECKSUMS
        +
IDEMPOTENCY
        +
BOUNDED ARCHIVE EXTRACTION
        +
CHUNKED PROCESSING
        +
ASYNC LARGE IMPORTS/EXPORTS
        +
RESOURCE AUTHORIZATION
        +
ENCRYPTION
        +
RETENTION
        +
CLEANUP
        +
OBSERVABILITY
        =
SAFE ENTERPRISE FILE PROCESSING
```

The governing principle is:

```text
Treat every uploaded file
as untrusted.

Bound its size.

Do not trust its name.

Do not trust its extension.

Do not trust its MIME type.

Do not load huge files
into memory.

Do not use pod disk
as permanent storage.

Use object storage
for durable large files.

Use short-lived,
narrowly scoped
presigned URLs.

Scan untrusted files
when risk requires it.

Quarantine before release.

Protect archive extraction.

Defend against Zip Slip.

Defend against ZIP bombs.

Parse CSV as CSV,
not as split text.

Stream large spreadsheets.

Do not evaluate
untrusted formulas.

Process imports
in bounded chunks.

Make imports restartable.

Make processing idempotent.

Bound error reports.

Authorize every download.

Expire temporary files.

Clean abandoned uploads.

Do not log file contents.

Do not put unnecessary PII
into storage keys.

And remember:

the safe size of a file
is not determined only
by how many bytes it has
on disk,

but by how much memory,
CPU, disk, network and
downstream work it can
force the system to consume.
```

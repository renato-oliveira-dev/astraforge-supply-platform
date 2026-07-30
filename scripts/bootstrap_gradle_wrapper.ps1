param(
    [string]$GradleVersion = "9.6.1",
    [string]$DistributionSha256 = "9c0f7faeeb306cb14e4279a3e084ca6b596894089a0638e68a07c945a32c9e14"
)

$ErrorActionPreference = "Stop"

$repositoryRoot = Split-Path -Parent $PSScriptRoot
$temporaryRoot = Join-Path ([System.IO.Path]::GetTempPath()) "enterprise-order-platform-gradle-wrapper"
$distributionFile = Join-Path $temporaryRoot "gradle-$GradleVersion-bin.zip"
$distributionDirectory = Join-Path $temporaryRoot "distribution"
$wrapperProject = Join-Path $temporaryRoot "wrapper-project"
$gradleUserHome = Join-Path $temporaryRoot "gradle-user-home"
$distributionUrl = "https://services.gradle.org/distributions/gradle-$GradleVersion-bin.zip"
$utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)

function Write-Utf8WithoutBom {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [Parameter(Mandatory = $true)]
        [string]$Content
    )

    [System.IO.File]::WriteAllText($Path, $Content, $utf8WithoutBom)
}

function Remove-TemporaryDirectory {
    if (-not (Test-Path $temporaryRoot)) {
        return
    }

    for ($attempt = 1; $attempt -le 5; $attempt++) {
        try {
            Remove-Item $temporaryRoot -Recurse -Force -ErrorAction Stop
            return
        }
        catch {
            if ($attempt -eq 5) {
                Write-Warning "Temporary directory could not be completely removed: $temporaryRoot"
                return
            }

            Start-Sleep -Milliseconds (500 * $attempt)
        }
    }
}

try {
    Remove-TemporaryDirectory

    New-Item -ItemType Directory -Path $temporaryRoot | Out-Null
    New-Item -ItemType Directory -Path $distributionDirectory | Out-Null
    New-Item -ItemType Directory -Path $wrapperProject | Out-Null
    New-Item -ItemType Directory -Path $gradleUserHome | Out-Null

    Write-Host "Downloading Gradle $GradleVersion from the official distribution service..."
    Invoke-WebRequest -Uri $distributionUrl -OutFile $distributionFile

    $actualChecksum = (Get-FileHash -Path $distributionFile -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualChecksum -ne $DistributionSha256.ToLowerInvariant()) {
        throw "Gradle distribution checksum mismatch. Expected $DistributionSha256 but found $actualChecksum."
    }

    Write-Host "Distribution checksum validated."
    Expand-Archive -Path $distributionFile -DestinationPath $distributionDirectory -Force

    Write-Utf8WithoutBom `
        -Path (Join-Path $wrapperProject "settings.gradle") `
        -Content "rootProject.name = 'wrapper-bootstrap'$([Environment]::NewLine)"
    Write-Utf8WithoutBom `
        -Path (Join-Path $wrapperProject "build.gradle") `
        -Content ""

    $gradleExecutable = Join-Path $distributionDirectory "gradle-$GradleVersion\bin\gradle.bat"
    $previousGradleUserHome = $env:GRADLE_USER_HOME
    $env:GRADLE_USER_HOME = $gradleUserHome

    try {
        & $gradleExecutable `
            --no-daemon `
            -p $wrapperProject `
            wrapper `
            --gradle-version $GradleVersion `
            --distribution-type bin

        if ($LASTEXITCODE -ne 0) {
            throw "Gradle wrapper generation failed with exit code $LASTEXITCODE."
        }
    }
    finally {
        $env:GRADLE_USER_HOME = $previousGradleUserHome
    }

    Copy-Item (Join-Path $wrapperProject "gradlew") $repositoryRoot -Force
    Copy-Item (Join-Path $wrapperProject "gradlew.bat") $repositoryRoot -Force
    New-Item -ItemType Directory -Path (Join-Path $repositoryRoot "gradle\wrapper") -Force | Out-Null
    Copy-Item `
        (Join-Path $wrapperProject "gradle\wrapper\gradle-wrapper.jar") `
        (Join-Path $repositoryRoot "gradle\wrapper") `
        -Force
    Copy-Item `
        (Join-Path $wrapperProject "gradle\wrapper\gradle-wrapper.properties") `
        (Join-Path $repositoryRoot "gradle\wrapper") `
        -Force

    $propertiesPath = Join-Path $repositoryRoot "gradle\wrapper\gradle-wrapper.properties"
    $properties = Get-Content $propertiesPath |
        Where-Object { $_ -notmatch '^distributionSha256Sum=' }
    $properties += "distributionSha256Sum=$DistributionSha256"
    [System.IO.File]::WriteAllLines($propertiesPath, $properties, [System.Text.Encoding]::ASCII)

    Write-Host "Gradle Wrapper $GradleVersion created successfully."
    Write-Host "Next command: .\gradlew.bat clean check --no-daemon --stacktrace"
}
finally {
    Remove-TemporaryDirectory
}

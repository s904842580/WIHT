<#
.SYNOPSIS
Runs WAHT backend tests and the frontend production build.

.DESCRIPTION
Use this script before a commit. It stops immediately and returns a non-zero code when a step fails.
#>
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot

Push-Location (Join-Path $projectRoot 'backend\waht-java')
try {
    & mvn test
    if ($LASTEXITCODE -ne 0) {
        throw "Backend tests failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}

Push-Location (Join-Path $projectRoot 'frontend\waht-web')
try {
    & npm.cmd run build
    if ($LASTEXITCODE -ne 0) {
        throw "Frontend build failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}

Write-Host 'WAHT backend tests and frontend build passed.' -ForegroundColor Green

<#
.SYNOPSIS
Runs WAHT Java/Python tests and the frontend production build.

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

$agentRoot = Join-Path $projectRoot 'backend\ai-service'
$agentPython = Join-Path $agentRoot '.venv\Scripts\python.exe'
if (-not (Test-Path $agentPython)) {
    throw 'Agent virtual environment is missing. Install backend/ai-service dependencies before verification.'
}

Push-Location $agentRoot
try {
    & $agentPython -m pytest -q
    if ($LASTEXITCODE -ne 0) {
        throw "Agent tests failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
}

Write-Host 'WAHT Java tests, frontend build, and Agent tests passed.' -ForegroundColor Green

<#
.SYNOPSIS
Starts the WAHT Spring Boot backend in the current terminal.

.DESCRIPTION
The selected port applies only to this process tree. Database settings are read from WAHT_DB_* variables.
#>
[CmdletBinding()]
param(
    [ValidateRange(1, 65535)]
    [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$backendDirectory = Join-Path $projectRoot 'backend\waht-java'

if ([string]::IsNullOrWhiteSpace($env:WAHT_DB_PASSWORD)) {
    throw 'WAHT_DB_PASSWORD is not configured. Run scripts\check-local.ps1 first.'
}

$previousPort = $env:WAHT_SERVER_PORT
$env:WAHT_SERVER_PORT = [string]$Port

Push-Location $backendDirectory
try {
    Write-Host ("Backend URL: http://localhost:{0}" -f $Port) -ForegroundColor Cyan
    & mvn spring-boot:run
    if ($LASTEXITCODE -ne 0) {
        throw "Maven failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
    $env:WAHT_SERVER_PORT = $previousPort
}

<#
.SYNOPSIS
Starts the WAHT Vite frontend in the current terminal.

.DESCRIPTION
The script sets an explicit frontend port and backend proxy target for this process tree.
#>
[CmdletBinding()]
param(
    [ValidateRange(1, 65535)]
    [int]$Port = 5173,
    [string]$ApiProxyTarget = 'http://localhost:8080'
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$frontendDirectory = Join-Path $projectRoot 'frontend\waht-web'

if (-not (Test-Path (Join-Path $frontendDirectory 'node_modules'))) {
    throw 'node_modules is missing. Run npm install in frontend\waht-web first.'
}

$previousProxyTarget = $env:VITE_API_PROXY_TARGET
$env:VITE_API_PROXY_TARGET = $ApiProxyTarget

Push-Location $frontendDirectory
try {
    Write-Host ("Frontend URL: http://127.0.0.1:{0}; API proxy: {1}" -f $Port, $ApiProxyTarget) -ForegroundColor Cyan
    & npm.cmd run dev -- --port $Port --strictPort
    if ($LASTEXITCODE -ne 0) {
        throw "Vite failed with exit code $LASTEXITCODE."
    }
} finally {
    Pop-Location
    $env:VITE_API_PROXY_TARGET = $previousProxyTarget
}

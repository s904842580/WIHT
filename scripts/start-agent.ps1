<#
.SYNOPSIS
Starts the WAHT Python Agent service in the current terminal.

.DESCRIPTION
The script uses backend/ai-service/.venv and does not read or write secret files.
#>
[CmdletBinding()]
param(
    [ValidateRange(1, 65535)]
    [int]$Port = 8000
)

$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$agentRoot = Join-Path $projectRoot 'backend\ai-service'
$python = Join-Path $agentRoot '.venv\Scripts\python.exe'

if (-not (Test-Path $python)) {
    throw 'Agent virtual environment is missing. Create .venv and install -e ".[dev]" first.'
}

Push-Location $agentRoot
try {
    & $python -m uvicorn waht_agent.main:app --host 127.0.0.1 --port $Port
} finally {
    Pop-Location
}

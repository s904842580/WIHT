<#
.SYNOPSIS
Checks commands, environment variables, and MySQL connectivity required by WAHT.

.DESCRIPTION
This script is read-only. It exits with a non-zero code when a required check fails.
#>
[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$script:HasFailure = $false

function Write-CheckResult {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [bool]$Passed,
        [Parameter(Mandatory = $true)]
        [string]$Detail
    )

    $status = if ($Passed) { 'OK' } else { 'FAIL' }
    $color = if ($Passed) { 'Green' } else { 'Red' }
    Write-Host ("[{0}] {1}: {2}" -f $status, $Name, $Detail) -ForegroundColor $color
    if (-not $Passed) {
        $script:HasFailure = $true
    }
}

function Test-CommandAvailable {
    param(
        [Parameter(Mandatory = $true)]
        [string]$CommandName
    )

    return $null -ne (Get-Command $CommandName -ErrorAction SilentlyContinue)
}

$javaAvailable = Test-CommandAvailable -CommandName 'java'
$javaDetail = if ($javaAvailable) { (& cmd.exe /d /c 'java -version 2>&1' | Select-Object -First 1) } else { 'Install JDK 17 and add it to PATH.' }
Write-CheckResult -Name 'Java' -Passed $javaAvailable -Detail $javaDetail

$mavenAvailable = Test-CommandAvailable -CommandName 'mvn'
$mavenDetail = if ($mavenAvailable) { (& mvn -version | Select-Object -First 1) } else { 'Install Maven and add it to PATH.' }
Write-CheckResult -Name 'Maven' -Passed $mavenAvailable -Detail $mavenDetail

$nodeAvailable = Test-CommandAvailable -CommandName 'node'
$nodeDetail = if ($nodeAvailable) { (& node --version) } else { 'Install Node.js 18 or newer.' }
Write-CheckResult -Name 'Node.js' -Passed $nodeAvailable -Detail $nodeDetail

$npmAvailable = Test-CommandAvailable -CommandName 'npm.cmd'
$npmDetail = if ($npmAvailable) { (& npm.cmd --version) } else { 'npm.cmd is not available in PATH.' }
Write-CheckResult -Name 'npm' -Passed $npmAvailable -Detail $npmDetail

$passwordConfigured = -not [string]::IsNullOrWhiteSpace($env:WAHT_DB_PASSWORD)
$passwordDetail = if ($passwordConfigured) { 'Configured.' } else { 'Not configured. Reopen the terminal or IDE after setting it.' }
Write-CheckResult -Name 'WAHT_DB_PASSWORD' -Passed $passwordConfigured -Detail $passwordDetail

$mysqlAvailable = Test-CommandAvailable -CommandName 'mysql'
if ($mysqlAvailable -and $passwordConfigured) {
    $dbUsername = if ([string]::IsNullOrWhiteSpace($env:WAHT_DB_USERNAME)) { 'root' } else { $env:WAHT_DB_USERNAME }
    $previousErrorActionPreference = $ErrorActionPreference
    $previousMysqlPassword = $env:MYSQL_PWD
    try {
        $ErrorActionPreference = 'SilentlyContinue'
        $env:MYSQL_PWD = $env:WAHT_DB_PASSWORD
        & mysql --connect-timeout=3 --user=$dbUsername --execute='SELECT 1;' 2>$null | Out-Null
        $mysqlExitCode = $LASTEXITCODE
    } finally {
        $env:MYSQL_PWD = $previousMysqlPassword
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $databaseConnected = $mysqlExitCode -eq 0
    $databaseDetail = if ($databaseConnected) { 'Connection succeeded.' } else { 'Connection failed. Check the service, username, and password.' }
    Write-CheckResult -Name 'MySQL' -Passed $databaseConnected -Detail $databaseDetail
} else {
    $mysqlDetail = if ($mysqlAvailable) { 'Installed. Configure the password to test connectivity.' } else { 'mysql is not available in PATH.' }
    Write-CheckResult -Name 'MySQL CLI' -Passed $mysqlAvailable -Detail $mysqlDetail
}

if ($script:HasFailure) {
    exit 1
}

Write-Host 'WAHT local environment checks passed.' -ForegroundColor Green

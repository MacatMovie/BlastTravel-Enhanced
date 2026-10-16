param(
    [Parameter(Mandatory=$true)][string]$Selection,
    [switch]$Clean
)
$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
$BuildScript = [System.IO.Path]::Combine($PSScriptRoot, 'build.ps1')
$LogsDir = [System.IO.Path]::Combine($Root, 'logs')
[void][System.IO.Directory]::CreateDirectory($LogsDir)
$Stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
$Map = @{
    A = 'forge-1.20.1'
    B = 'neoforge-1.21.1'
    C = 'fabric-1.20.1'
    D = 'fabric-1.21.1'
}
$Allowed = @('forge-1.20.1','neoforge-1.21.1','fabric-1.20.1','fabric-1.21.1')
$Tokens = @($Selection -split '[,\s]+' | Where-Object { $_.Trim().Length -gt 0 })
if ($Tokens.Count -eq 0) {
    Write-Host 'No build targets selected.' -ForegroundColor Red
    exit 1
}
$Failures = @()
$Successes = 0
Write-Host ''
Write-Host "Selected: $Selection" -ForegroundColor Cyan
foreach ($Token in $Tokens) {
    $Key = $Token.ToUpperInvariant()
    if ($Map.ContainsKey($Key)) { $Target = $Map[$Key] }
    elseif ($Allowed -contains $Token.ToLowerInvariant()) { $Target = $Token.ToLowerInvariant() }
    else {
        Write-Host "Unknown selection: $Token" -ForegroundColor Red
        $Failures += $Token
        continue
    }
    $LogPath = [System.IO.Path]::Combine($LogsDir, "build-$Target-$Stamp.log")
    $Latest = [System.IO.Path]::Combine($LogsDir, 'build-one-latest.log')
    Write-Host ''
    Write-Host "============================================================"
    Write-Host "BUILDING: $Target" -ForegroundColor Cyan
    Write-Host "Log: $LogPath"
    Write-Host "============================================================"
    $Arguments = @('-NoProfile','-ExecutionPolicy','Bypass','-File',$BuildScript,'-Target',$Target,'-LogPath',$LogPath)
    if ($Clean) { $Arguments += '-Clean' }
    & powershell.exe @Arguments
    $Code = $LASTEXITCODE
    if ([System.IO.File]::Exists($LogPath)) {
        [System.IO.File]::Copy($LogPath, $Latest, $true)
    }
    if ($Code -eq 0) {
        Write-Host "=== $Target SUCCESS ===" -ForegroundColor Green
        $Successes++
    }
    else {
        Write-Host "=== $Target FAILED (exit code $Code) ===" -ForegroundColor Red
        $Failures += $Target
    }
}
Write-Host ''
Write-Host '============================================================'
Write-Host "Successful targets: $Successes"
if ($Failures.Count -gt 0) {
    Write-Host "Failed targets: $($Failures -join ', ')" -ForegroundColor Red
    Write-Host '============================================================'
    exit 1
}
Write-Host "Finished jars: $([System.IO.Path]::Combine($Root,'dist'))" -ForegroundColor Green
Write-Host '============================================================'
exit 0

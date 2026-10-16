# Blast Travel multi-target build and jar collector.
# Compatible with Windows PowerShell 5.1, including parent folders with
# wildcard metacharacters such as [ and ]. No transcript host is required.
param(
    [Parameter(Position=0)][string]$Target,
    [switch]$Clean,
    [switch]$All,
    [switch]$CollectOnly,
    [string]$LogPath
)

$ErrorActionPreference = 'Stop'
$Root = Split-Path -Parent $PSScriptRoot
$TargetsDir = [System.IO.Path]::Combine($Root, 'targets')
$OutputDir = [System.IO.Path]::Combine($Root, 'dist')
[void][System.IO.Directory]::CreateDirectory($OutputDir)

# Log directly to a file instead of Start-Transcript, which isn't supported
# by every PowerShell host. Every printed line, including Gradle output, is
# mirrored into the supplied log file as the build progresses.
$script:LogFile = $null
if (-not [string]::IsNullOrWhiteSpace($LogPath)) {
    $script:LogFile = [System.IO.Path]::GetFullPath($LogPath)
    [void][System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($script:LogFile))
    [System.IO.File]::WriteAllText($script:LogFile, '', ([System.Text.UTF8Encoding]::new($false)))
}

function Say([string]$Message, [string]$Color = 'Gray') {
    Write-Host $Message -ForegroundColor $Color
    if ($script:LogFile) {
        [System.IO.File]::AppendAllText($script:LogFile, $Message + [Environment]::NewLine)
    }
}

function Show-MatrixProgress([int]$Current, [int]$Total, [string]$Name) {
    if ($Total -le 0) { return }
    $width = 30
    $filled = [Math]::Floor(($Current / $Total) * $width)
    if ($filled -gt $width) { $filled = $width }
    $bar = ('#' * $filled) + ('-' * ($width - $filled))
    $pct = [Math]::Floor(($Current / $Total) * 100)
    Say ''
    Say ("[{0}] {1,3}%  ({2}/{3})  {4}" -f $bar, $pct, $Current, $Total, $Name) 'Cyan'
}

function Collect-TargetJars([string]$Name) {
    $libs = [System.IO.Path]::Combine($TargetsDir, $Name, 'build', 'libs')
    if (-not [System.IO.Directory]::Exists($libs)) {
        Say "No existing build output for $Name" 'Yellow'
        return 0
    }

    $count = 0
    foreach ($sourceJar in [System.IO.Directory]::GetFiles($libs, 'BlastTravel-*.jar')) {
        $jarName = [System.IO.Path]::GetFileName($sourceJar)
        if ($jarName -match '(?i)(sources|dev|shadow)') { continue }
        $destination = [System.IO.Path]::Combine($OutputDir, $jarName)
        [System.IO.File]::Copy($sourceJar, $destination, $true)
        Say "Collected: $jarName" 'DarkGray'
        $count++
    }
    return $count
}

function Build-Target([string]$Name) {
    $dir = [System.IO.Path]::Combine($TargetsDir, $Name)
    $launcher = [System.IO.Path]::Combine($dir, 'gradlew.bat')

    # Don't use Test-Path -Path or Push-Location -Path here; a folder name
    # like "Blast Travel [NeoForge Port]" would be interpreted as a wildcard.
    if (-not [System.IO.File]::Exists($launcher)) {
        throw "Missing launcher for '$Name': $launcher"
    }

    Say ''
    Say "=== $Name ===" 'Cyan'
    Push-Location -LiteralPath $dir
    try {
        $gradleArgs = @('--console=plain', '--no-daemon', '--no-watch-fs')
        if ($Clean) { $gradleArgs += 'clean' }
        $gradleArgs += 'build'
        Say "Running: gradlew.bat $($gradleArgs -join ' ')" 'DarkGray'

        # CMD's call command handles quoted batch paths containing spaces,
        # square brackets, and other non-wildcard punctuation. Merge native
        # stderr in CMD so Windows PowerShell 5.1 can stream it alongside stdout.
        $command = 'call "' + $launcher + '" ' + ($gradleArgs -join ' ') + ' 2>&1'
        & cmd.exe /d /c $command | ForEach-Object { Say ([string]$_) }
        $exitCode = $LASTEXITCODE
        if ($exitCode -ne 0) {
            throw "$Name build failed with exit code $exitCode"
        }

        [void](Collect-TargetJars $Name)
    }
    finally {
        Pop-Location
    }
}

$allTargets = @('forge-1.20.1', 'neoforge-1.21.1', 'fabric-1.20.1', 'fabric-1.21.1')

try {
    if ($CollectOnly) {
        Say 'Collecting existing target JARs (no compilation)...' 'Cyan'
        $totalCollected = 0
        foreach ($t in $allTargets) { $totalCollected += (Collect-TargetJars $t) }
        Say "Collected $totalCollected jar(s) into: $OutputDir" 'Green'
        exit 0
    }

    if ($All) {
        if ($Clean -and [System.IO.Directory]::Exists($OutputDir)) {
            Remove-Item -LiteralPath $OutputDir -Recurse -Force
            [void][System.IO.Directory]::CreateDirectory($OutputDir)
        }

        $failures = @()
        $total = $allTargets.Count
        $i = 0
        foreach ($t in $allTargets) {
            $i++
            Show-MatrixProgress $i $total $t
            try {
                Build-Target $t
                Say "=== $t SUCCESS ===" 'Green'
            }
            catch {
                $failures += $t
                Say "=== $t FAILED ===" 'Red'
                Say $_.Exception.Message 'Red'
            }
        }

        Say ''
        Say '============================================================'
        if ($failures.Count -eq 0) {
            Say '[##############################] 100%  ALL TARGETS BUILT SUCCESSFULLY' 'Green'
            Say "Output: $OutputDir"
            exit 0
        }

        Say ("BUILD MATRIX FINISHED WITH {0} FAILURE(S):" -f $failures.Count) 'Red'
        foreach ($f in $failures) { Say "  - $f" 'Red' }
        Say ("Build failed for: " + ($failures -join ', ')) 'Red'
        exit 1
    }

    if (-not $Target) {
        Say 'Available targets:'
        foreach ($t in $allTargets) { Say "  $t" }
        Say ''
        Say 'Usage: .\scripts\build.ps1 neoforge-1.21.1 [-Clean]'
        Say '       .\scripts\build.ps1 -All [-Clean]'
        Say '       .\scripts\build.ps1 -CollectOnly'
        exit 0
    }

    if ($allTargets -notcontains $Target) {
        Say "Unknown target: '$Target'. Valid targets: $($allTargets -join ', ')" 'Red'
        exit 1
    }

    Show-MatrixProgress 1 1 $Target
    Build-Target $Target
    Say "[##############################] 100%  $Target BUILT SUCCESSFULLY" 'Green'
    exit 0
}
catch {
    Say ('ERROR: ' + $_.Exception.Message) 'Red'
    exit 1
}

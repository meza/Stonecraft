param(
    [string[]] $Projects = @(),
    [switch] $StopOnFailure,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]] $GradleArgs = @()
)

$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$gradle = Join-Path $repoRoot "gradlew.bat"
$versionsDir = Join-Path $repoRoot "versions"

if (-not (Test-Path -LiteralPath $gradle)) {
    throw "Could not find Gradle wrapper at $gradle"
}

if ($Projects.Count -eq 0) {
    $Projects = Get-ChildItem -LiteralPath $versionsDir -Directory |
        Where-Object { $_.Name -ne "dependencies" } |
        Sort-Object @{ Expression = { [version](($_.Name -split "-")[0]) } }, @{ Expression = { ($_.Name -split "-")[1] } } |
        Select-Object -ExpandProperty Name
}

$failures = New-Object System.Collections.Generic.List[string]
$startedAt = Get-Date

Push-Location $repoRoot
try {
    foreach ($project in $Projects) {
        $task = ":${project}:runClient"
        $startedProjectAt = Get-Date

        Write-Host ""
        Write-Host "============================================================"
        Write-Host "Starting $task"
        Write-Host "Started at $($startedProjectAt.ToString("yyyy-MM-dd HH:mm:ss"))"
        Write-Host "============================================================"

        & $gradle $task @GradleArgs
        $exitCode = $LASTEXITCODE

        $finishedProjectAt = Get-Date
        $duration = $finishedProjectAt - $startedProjectAt

        if ($exitCode -eq 0) {
            Write-Host "Finished $task successfully after $($duration.ToString())"
        } else {
            $message = "$task failed with exit code $exitCode after $($duration.ToString())"
            Write-Host $message
            $failures.Add($message)

            if ($StopOnFailure) {
                exit $exitCode
            }
        }
    }
} finally {
    Pop-Location
}

$finishedAt = Get-Date
$totalDuration = $finishedAt - $startedAt

Write-Host ""
Write-Host "============================================================"
Write-Host "Client run sequence finished after $($totalDuration.ToString())"
Write-Host "============================================================"

if ($failures.Count -gt 0) {
    Write-Host ""
    Write-Host "Failures:"
    foreach ($failure in $failures) {
        Write-Host " - $failure"
    }
    exit 1
}

exit 0

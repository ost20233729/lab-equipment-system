param(
    [string]$DriveLetter = "X"
)

$ErrorActionPreference = "Stop"
$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$driveName = $DriveLetter.TrimEnd(':')
$drivePath = "$driveName`:"
$createdMapping = $false

function Remove-TempDrive {
    if ($createdMapping) {
        cmd /c "subst $drivePath /D >nul 2>nul" | Out-Null
    }
}

try {
    $existingDrive = Get-PSDrive -Name $driveName -ErrorAction SilentlyContinue
    if ($existingDrive) {
        throw "Drive $drivePath is already in use. Re-run with another drive letter, e.g. -DriveLetter W"
    }

    cmd /c "subst $drivePath `"$projectRoot`"" | Out-Null
    $createdMapping = $true
    Push-Location "$drivePath\"
    try {
        mvn spring-boot:run "-Dspring-boot.run.profiles=dev" "-Dspring-boot.run.useTestClasspath=true"
    } finally {
        Pop-Location
    }
} finally {
    Remove-TempDrive
}

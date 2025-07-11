# PowerShell script to help create a GitHub release for SiMP3
# This script calculates the SHA-256 checksum and provides instructions

param(
    [Parameter(Mandatory=$true)]
    [string]$Version,
    
    [Parameter(Mandatory=$false)]
    [string]$ExePath = ".\target\SiMP3.exe"
)

Write-Host "SiMP3 Release Helper Script" -ForegroundColor Green
Write-Host "===========================" -ForegroundColor Green
Write-Host ""

# Check if the executable exists
if (-not (Test-Path $ExePath)) {
    Write-Host "Error: Executable not found at $ExePath" -ForegroundColor Red
    Write-Host "Please build the project first with: mvn clean package" -ForegroundColor Yellow
    exit 1
}

# Calculate SHA-256 checksum
Write-Host "Calculating SHA-256 checksum..." -ForegroundColor Yellow
$hash = Get-FileHash -Path $ExePath -Algorithm SHA256
$checksum = $hash.Hash

Write-Host "Checksum calculated successfully!" -ForegroundColor Green
Write-Host ""

# Get file size
$fileInfo = Get-Item $ExePath
$fileSizeMB = [math]::Round($fileInfo.Length / 1MB, 2)

# Generate release notes template
$releaseNotes = @"
## What's New in v$Version

- [Add your changes here]
- [Add more changes]
- [Bug fixes, features, etc.]

## Installation

1. Download `SiMP3.exe` from the assets below
2. Replace your existing SiMP3.exe with the new version
3. Run the application

## System Requirements

- Windows 10/11
- Java 17 or higher (if using the JAR version)

## Checksum

SHA-256: ``$checksum``

## File Information

- File: SiMP3.exe
- Size: $fileSizeMB MB
"@

# Save release notes to file
$releaseNotesPath = ".\release-notes-v$Version.md"
$releaseNotes | Out-File -FilePath $releaseNotesPath -Encoding UTF8

Write-Host "Release Information:" -ForegroundColor Cyan
Write-Host "===================" -ForegroundColor Cyan
Write-Host "Version:    v$Version" -ForegroundColor White
Write-Host "File:       $ExePath" -ForegroundColor White
Write-Host "Size:       $fileSizeMB MB" -ForegroundColor White
Write-Host "SHA-256:    $checksum" -ForegroundColor White
Write-Host ""

Write-Host "Release notes template saved to: $releaseNotesPath" -ForegroundColor Green
Write-Host ""

Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Edit the release notes in $releaseNotesPath" -ForegroundColor White
Write-Host "2. Go to https://github.com/YOUR_USERNAME/YOUR_REPO/releases/new" -ForegroundColor White
Write-Host "3. Create a new release with:" -ForegroundColor White
Write-Host "   - Tag: v$Version" -ForegroundColor White
Write-Host "   - Title: SiMP3 v$Version" -ForegroundColor White
Write-Host "   - Description: Copy from $releaseNotesPath" -ForegroundColor White
Write-Host "   - Asset: Upload $ExePath" -ForegroundColor White
Write-Host ""
Write-Host "Remember to update UpdateConfig.java with your GitHub username and repository!" -ForegroundColor Red
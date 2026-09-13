param(
    [Parameter(Mandatory=$true)]
    [ValidatePattern('^submission-03(?:-r[1-9][0-9]*)?$')]
    [string]$Tag
)
$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
$exportRoot = Join-Path $projectRoot 'submissions'
$destination = Join-Path $exportRoot ("Bai_03_" + $Tag)
if (Test-Path -LiteralPath $destination) { throw "Folder exists; not overwriting: $destination" }
$sha = & git -c "safe.directory=$projectRoot" -C $projectRoot rev-parse --verify "refs/tags/$Tag^{commit}"
if ($LASTEXITCODE -ne 0) { throw "Tag not found: $Tag" }
New-Item -ItemType Directory -Path $exportRoot -Force | Out-Null
$archive = Join-Path $exportRoot ("$Tag-source.tar")
if (Test-Path -LiteralPath $archive) { throw "Archive exists; not overwriting: $archive" }
& git -c "safe.directory=$projectRoot" -C $projectRoot archive --format=tar -o $archive $sha
if ($LASTEXITCODE -ne 0) { throw 'Git archive failed' }
New-Item -ItemType Directory -Path $destination | Out-Null
& tar -xf $archive -C $destination
if ($LASTEXITCODE -ne 0) { throw "Extraction failed; incomplete folder: $destination" }
Write-Host "Source: $destination"
Write-Host "Tag: $Tag; commit: $sha"
Write-Host 'Export is not acceptance proof. Review BAI_03_CHECKLIST.md before submission.'

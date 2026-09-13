$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
$exportRoot = Join-Path $projectRoot 'submissions'
$archiveRoot = Join-Path $projectRoot '.tools'
$milestones = @{'submission-01'='Bai_01_Servlet_JDBC'; 'submission-02'='Bai_02_Servlet_JPA'; 'submission-03'='Bai_03_OTP_Product'}
Push-Location $projectRoot
try {
    New-Item -ItemType Directory -Path $exportRoot,$archiveRoot -Force | Out-Null
    foreach($tag in ($milestones.Keys | Sort-Object)) {
        $destination = [IO.Path]::GetFullPath((Join-Path $exportRoot $milestones[$tag]))
        if (-not $destination.StartsWith($exportRoot + [IO.Path]::DirectorySeparatorChar,[StringComparison]::OrdinalIgnoreCase)) { throw 'Unsafe destination' }
        if (Test-Path -LiteralPath $destination) { throw "Folder exists, will not overwrite: $destination" }
        & git -c "safe.directory=$projectRoot" rev-parse --verify "$tag^{commit}"
        if ($LASTEXITCODE -ne 0) { throw "Missing tag: $tag" }
        $archive = Join-Path $archiveRoot "$tag.tar"
        & git -c "safe.directory=$projectRoot" archive --format=tar -o $archive $tag
        if ($LASTEXITCODE -ne 0) { throw "Archive failed: $tag" }
        New-Item -ItemType Directory -Path $destination | Out-Null
        & tar -xf $archive -C $destination
        if ($LASTEXITCODE -ne 0) { throw "Extraction failed: $tag" }
        Write-Host "$tag exported to $destination"
    }
} finally { Pop-Location }

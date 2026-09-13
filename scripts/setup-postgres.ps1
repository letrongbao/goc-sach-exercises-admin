$ErrorActionPreference = 'Stop'
$projectRoot = [IO.Path]::GetFullPath((Split-Path $PSScriptRoot -Parent))
$configPath = Join-Path $projectRoot 'config/local.properties'
$containerName = 'utex-bookstore-postgres-17'
$volumeName = 'utex-bookstore-pg17-data'
$imageName = 'postgres:17.11-alpine'

function Read-SecretText([string]$Prompt) {
    $secureValue = Read-Host $Prompt -AsSecureString
    $pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secureValue)
    try { return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer) }
    finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer); $secureValue.Dispose() }
}
function Escape-Property([string]$Value) {
    return $Value.Replace('\', '\\').Replace("`r", '\r').Replace("`n", '\n').Replace("`t", '\t').Replace(' ', '\ ')
}
try {
    & docker info --format '{{.ServerVersion}}' | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'Open Docker Desktop first.' }
    $names = & docker ps -a --format '{{.Names}}'
    if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect Docker containers.' }
    if ($names -contains $containerName) { throw 'Bookstore container already exists. No changes made; ask to check it.' }
    $volumes = & docker volume ls --format '{{.Name}}'
    if ($LASTEXITCODE -ne 0) { throw 'Cannot inspect Docker volumes.' }
    if ($volumes -contains $volumeName) { throw 'Bookstore volume already exists. Refusing to reuse existing data.' }
    if (Get-NetTCPConnection -State Listen -LocalPort 5434 -ErrorAction SilentlyContinue) { throw 'Port 5434 is busy.' }
    if (-not (Test-Path -LiteralPath $configPath)) { throw 'Missing config/local.properties.' }
    $configLines = [IO.File]::ReadAllLines($configPath)
    foreach ($key in @('db.url','db.user','db.password','otp.secret')) {
        if (@($configLines | Where-Object { $_ -match ('^' + [regex]::Escape($key) + '=') }).Count -ne 1) {
            throw "Config must contain exactly one $key line."
        }
    }
    & docker image inspect $imageName --format '{{.Id}}' | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'PostgreSQL image has not been downloaded.' }
    Write-Host 'BAI 03 - PostgreSQL rieng (Docker), cong 5434'
    Write-Host 'Tao database rong bookstore_03; KHONG chay schema/migration bai tap.'
    Write-Host 'Mat khau chi luu tren may (Docker va config/local.properties), khong gui vao chat.'
    Write-Host 'Tai khoan bookstore la quan tri cua instance demo rieng, khong dung production.'
    $passwordValue = Read-SecretText 'Tu dat mat khau PostgreSQL (it nhat 12 ky tu)'
    $confirmValue = Read-SecretText 'Nhap lai mat khau'
    if ($passwordValue.Length -lt 12 -or $passwordValue -ne $confirmValue) { throw 'Password too short or confirmation differs. No database created.' }
    if (Test-Path Env:POSTGRES_PASSWORD) { throw 'POSTGRES_PASSWORD already exists in this process; refusing to overwrite.' }
    $env:POSTGRES_PASSWORD = $passwordValue
    try {
        & docker run -d --name $containerName --restart unless-stopped -p '127.0.0.1:5434:5432' -e POSTGRES_PASSWORD -e POSTGRES_USER=bookstore -e POSTGRES_DB=bookstore_03 -v "${volumeName}:/var/lib/postgresql/data" $imageName | Out-Null
        if ($LASTEXITCODE -ne 0) { throw 'Docker start failed. Do not delete data; ask for inspection.' }
    } finally { [Environment]::SetEnvironmentVariable('POSTGRES_PASSWORD', $null, 'Process') }
    $ready = $false
    for ($attempt = 0; $attempt -lt 30; $attempt++) {
        & docker exec $containerName pg_isready -U bookstore -d bookstore_03 *> $null
        if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        Start-Sleep -Seconds 1
    }
    if (-not $ready) { throw 'Database not ready yet. Container retained for inspection.' }
    $configLines = $configLines | ForEach-Object {
        if ($_ -match '^db.url=') { 'db.url=jdbc:postgresql://127.0.0.1:5434/bookstore_03' }
        elseif ($_ -match '^db.user=') { 'db.user=bookstore' }
        elseif ($_ -match '^db.password=') { 'db.password=' + (Escape-Property $passwordValue) }
        elseif ($_ -match '^otp.secret=REPLACE_') {
            $randomBytes = New-Object byte[] 32
            $generator = [Security.Cryptography.RandomNumberGenerator]::Create()
            try { $generator.GetBytes($randomBytes) } finally { $generator.Dispose() }
            'otp.secret=' + [Convert]::ToBase64String($randomBytes)
        } else { $_ }
    }
    [IO.File]::WriteAllLines($configPath, $configLines, (New-Object Text.UTF8Encoding($false)))
    Write-Host 'OK: PostgreSQL ready; local.properties updated. SMTP fields preserved.' -ForegroundColor Green
    Write-Host 'DBeaver: Host 127.0.0.1 | Port 5434 | Database bookstore_03 | User bookstore'
    Write-Host 'Use the password you just entered. No application tables have been created.'
} catch {
    Write-Host $_.Exception.Message -ForegroundColor Red
} finally {
    $passwordValue = $null
    $confirmValue = $null
    Read-Host 'Nhan Enter de dong cua so' | Out-Null
}

$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    & ./mvnw.cmd -q test-compile exec:java '-Dexec.mainClass=vn.edu.utex.bookstore.PreviewServer' '-Dexec.classpathScope=test'
    if ($LASTEXITCODE -ne 0) { throw 'Preview failed' }
} finally { Pop-Location }

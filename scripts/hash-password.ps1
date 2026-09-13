$ErrorActionPreference = 'Stop'
Push-Location (Split-Path $PSScriptRoot -Parent)
try {
    & ./mvnw.cmd -q compile dependency:copy-dependencies '-DincludeScope=runtime'
    if ($LASTEXITCODE -ne 0) { throw 'Build failed' }
    & java -cp 'target/classes;target/dependency/*' vn.edu.utex.bookstore.auth.HashPassword
} finally { Pop-Location }

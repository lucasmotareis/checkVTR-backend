param(
    [Parameter(Mandatory=$true)]
    [ValidateSet('https://back_homolog.pmto8bpm.com.br', 'https://homolog_back.pmto8bpm.com.br')]
    [string]$BaseUrl,
    [ValidateSet('smoke', 'baseline', 'apm', 'unauthenticated')]
    [string]$Mode = 'smoke',
    [string]$K6Path = 'k6'
)

$ErrorActionPreference = 'Stop'
$apmRepo = Split-Path -Parent $PSScriptRoot
$apmK6 = Get-Command $K6Path -ErrorAction SilentlyContinue
if (-not $apmK6) {
    throw 'k6 nao encontrado. Instale o k6 oficial ou informe -K6Path com o caminho do executavel portatil.'
}
$apmVariables = @('BASE_URL', 'CONFIRMED_HOMOLOG_URL', 'MODE', 'TEST_MATRICULA', 'TEST_SENHA')
$apmPrevious = @{}
foreach ($apmName in $apmVariables) {
    $apmPrevious[$apmName] = [Environment]::GetEnvironmentVariable($apmName, 'Process')
}

try {
    [Environment]::SetEnvironmentVariable('BASE_URL', $BaseUrl, 'Process')
    [Environment]::SetEnvironmentVariable('CONFIRMED_HOMOLOG_URL', $BaseUrl, 'Process')
    [Environment]::SetEnvironmentVariable('MODE', $Mode, 'Process')
    if ($Mode -ne 'unauthenticated') {
        $apmMatricula = Read-Host 'Matricula do usuario ficticio de homologacao'
        $apmSecurePassword = Read-Host 'Senha do usuario ficticio (oculta)' -AsSecureString
        [Environment]::SetEnvironmentVariable('TEST_MATRICULA', $apmMatricula, 'Process')
        [Environment]::SetEnvironmentVariable('TEST_SENHA', ([System.Net.NetworkCredential]::new('', $apmSecurePassword)).Password, 'Process')
    }
    New-Item -ItemType Directory -Force -Path (Join-Path $apmRepo 'observability/results') | Out-Null
    Push-Location $apmRepo
    try {
        & $apmK6.Source run --new-machine-readable-summary=false observability/k6/checkvtr.js
        $apmExitCode = $LASTEXITCODE
        $apmResult = Join-Path $apmRepo "observability/results/$Mode.json"
        if (Test-Path -LiteralPath $apmResult) {
            $apmStamp = Get-Date -Format 'yyyyMMdd-HHmmss'
            Copy-Item -LiteralPath $apmResult -Destination (Join-Path $apmRepo "observability/results/$Mode-$apmStamp.json")
        }
        if ($apmExitCode -ne 0) { throw "k6 encerrou com codigo $apmExitCode. Conferir falhas ou interrupcao antes de concluir o experimento." }
    } finally { Pop-Location }
} finally {
    foreach ($apmName in $apmVariables) {
        [Environment]::SetEnvironmentVariable($apmName, $apmPrevious[$apmName], 'Process')
    }
    $apmSecurePassword = $null
    $apmMatricula = $null
}

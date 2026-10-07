param(
    [Parameter(Mandatory=$true)][string]$DocxPath,
    [string]$PdfPath
)
$ErrorActionPreference = 'Stop'
$apmSource = (Resolve-Path -LiteralPath $DocxPath).Path
if ([IO.Path]::GetExtension($apmSource) -ne '.docx') { throw 'Informe um arquivo DOCX.' }
if (-not $PdfPath) { $PdfPath = [IO.Path]::ChangeExtension($apmSource, '.pdf') }
$apmTarget = [IO.Path]::GetFullPath($PdfPath)
if ([IO.Path]::GetExtension($apmTarget) -ne '.pdf') { throw 'O destino precisa ter extensao PDF.' }
$apmTempPdf = Join-Path (Split-Path -Parent $apmTarget) ('.apm-export-' + [Guid]::NewGuid().ToString('N') + '.pdf')
$apmWord = $null
$apmDocument = $null
try {
    # Instancia propria em segundo plano, sem macros, sem abrir janela ou salvar o DOCX.
    $apmWord = New-Object -ComObject Word.Application
    $apmWord.Visible = $false
    $apmWord.DisplayAlerts = 0
    $apmWord.AutomationSecurity = 3
    $apmDocument = $apmWord.Documents.Open($apmSource, $false, $true, $false)
    $apmDocument.ExportAsFixedFormat($apmTempPdf, 17)
    $apmDocument.Close(0)
    $apmDocument = $null
    Move-Item -LiteralPath $apmTempPdf -Destination $apmTarget -Force
    Write-Output $apmTarget
} finally {
    if ($apmDocument) { $apmDocument.Close(0) }
    if ($apmWord) {
        $apmWord.Quit(0)
        [System.Runtime.InteropServices.Marshal]::ReleaseComObject($apmWord) | Out-Null
    }
    if (Test-Path -LiteralPath $apmTempPdf) { Remove-Item -LiteralPath $apmTempPdf }
}

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$gradlePropsPath = Join-Path $projectRoot "gradle.properties"

if (-not (Test-Path $gradlePropsPath)) {
  throw "gradle.properties not found: $gradlePropsPath"
}

$content = Get-Content $gradlePropsPath -Raw

if ($content -match "(?m)^useLocalZuiComposeDesktop=") {
  $content = [regex]::Replace($content, "(?m)^useLocalZuiComposeDesktop=.*$", "useLocalZuiComposeDesktop=true")
} else {
  $content += "`r`nuseLocalZuiComposeDesktop=true"
}

Set-Content -Path $gradlePropsPath -Value $content -NoNewline
Write-Host "Switched to local zui-compose-desktop (useLocalZuiComposeDesktop=true)."

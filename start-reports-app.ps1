$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
$DemoPath = Join-Path $Root "demo1-github"

Start-Process powershell -WindowStyle Hidden -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location -LiteralPath '$DemoPath'; cmd /c mvnw.cmd spring-boot:run"
)

Start-Sleep -Seconds 5

Start-Process powershell -WindowStyle Hidden -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location -LiteralPath '$Root'; python -m uvicorn main:app --host 127.0.0.1 --port 8000 --reload"
)

Write-Host "Reports app starting at http://127.0.0.1:8000"

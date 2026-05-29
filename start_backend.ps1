param([string]$jarPath, [string]$port, [int]$wait=30)
$start = Get-Date; $failed = $false; $proc = $null
try {
    $proc = Start-Process java -ArgumentList "-jar", $jarPath, "--server.port=$port" -PassThru -NoNewWindow
    Write-Host "Started PID: $($proc.Id)"
    for ($i = 0; $i -lt $wait; $i++) {
        Start-Sleep 1
        if ($proc.HasExited) { Write-Host "[ERROR] Process exited"; $failed = $true; break }
        $conn = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue | Where-Object { $_.State -eq 'Listen' }
        if ($conn) { Write-Host "[OK] Port $port is listening"; break }
        if ($i % 5 -eq 0) { Write-Host "Waiting... $($i)s" }
    }
    if (!$proc.HasExited -and !$conn) { Write-Host "[WARN] Port not ready after $wait seconds" }
} catch {
    Write-Host "[ERROR] $_"
    $failed = $true
}
if ($proc -and !$proc.HasExited -and !$failed) {
    Write-Host "[READY] Backend is running on port $port"
} else {
    Write-Host "[FAILED] Backend failed to start"
    if ($proc -and !$proc.HasExited) { Stop-Process $proc.Id -Force -ErrorAction SilentlyContinue }
}
exit [int]$failed
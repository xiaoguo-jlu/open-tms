$env:JAVA_HOME = 'D:\Program Files\Java\jdk-21.0.11'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

Write-Host "Starting application..."
$process = Start-Process -FilePath 'java.exe' -ArgumentList '-jar', 'target\opentms-basedata-1.0.0-SNAPSHOT.jar' -WorkingDirectory 'E:\code-project\open-tms\open-tms\basedata' -NoNewWindow -PassThru

Write-Host "Waiting for startup..."
Start-Sleep -Seconds 18

Write-Host "Testing endpoint..."
try {
    $result = Invoke-RestMethod -Uri 'http://localhost:8081/opentms/basedata/api/v1/countries/page?pageNum=1&pageSize=5' -Method GET -TimeoutSec 10
    Write-Host "SUCCESS!"
    $result | ConvertTo-Json -Depth 3
} catch {
    Write-Host "FAILED: $($_.Exception.Message)"
}

Start-Sleep -Seconds 1
Write-Host "Done"
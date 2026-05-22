$env:JAVA_HOME = 'D:\Program Files\Java\jdk-21.0.11'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"
$ErrorActionPreference = 'Continue'

Write-Host "Starting Spring Boot application..."
$process = Start-Process -FilePath 'java.exe' -ArgumentList '-jar', 'target\opentms-basedata-1.0.0-SNAPSHOT.jar' -WorkingDirectory 'E:\code-project\open-tms\open-tms\basedata' -NoNewWindow -PassThru

Write-Host "Process started with PID: $($process.Id)"
Start-Sleep -Seconds 15

Write-Host "Testing endpoints..."
$endpoints = @(
    'http://localhost:8081/v1/api/v1/currencies',
    'http://localhost:8081/v1/api/v1/banks',
    'http://localhost:8081/v1/api/v1/countries'
)

foreach ($endpoint in $endpoints) {
    try {
        Write-Host "`nTesting: $endpoint"
        $response = Invoke-WebRequest -Uri $endpoint -Method GET -TimeoutSec 5 -ErrorAction Stop
        Write-Host "SUCCESS! Status: $($response.StatusCode)"
    } catch {
        Write-Host "FAILED: $($_.Exception.Message)"
    }
}
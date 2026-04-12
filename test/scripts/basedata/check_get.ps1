$r = Invoke-WebRequest -Uri 'http://localhost:8081/api/v1/banks/page' -Method GET -UseBasicParsing
Write-Host $r.StatusCode
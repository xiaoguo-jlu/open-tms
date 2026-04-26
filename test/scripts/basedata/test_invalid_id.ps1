$baseUrl = "http://localhost:8081/api/v1"

Write-Host "=== Test Invalid ID Parameters ==="

Write-Host "`n1. Test GET with string ID (abc):"
try {
    $r = Invoke-WebRequest -Uri "$baseUrl/currencies/abc" -Method GET -UseBasicParsing -ErrorAction Stop
    Write-Host "Status: $($r.StatusCode), Response: $($r.Content)"
} catch {
    Write-Host "Status: $($_.Exception.Response.StatusCode.Value__), Response: $($_.Exception.Message)"
}

Write-Host "`n2. Test DELETE with negative ID (-1):"
try {
    $r = Invoke-WebRequest -Uri "$baseUrl/currencies/-1" -Method DELETE -UseBasicParsing -ErrorAction Stop
    Write-Host "Status: $($r.StatusCode), Response: $($r.Content)"
} catch {
    Write-Host "Status: $($_.Exception.Response.StatusCode.Value__), Response: $($_.Exception.Message)"
}
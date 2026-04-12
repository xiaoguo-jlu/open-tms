$baseUrl = "http://localhost:8081/api/v1"

function Test-Post {
    param($path, $body, $desc)
    try {
        $r = Invoke-WebRequest -Uri "$baseUrl$path" -Method POST -ContentType "application/json" -Body $body -UseBasicParsing -ErrorAction Stop
        Write-Host "[$desc] SUCCESS - Status: $($r.StatusCode)"
    } catch {
        $status = $_.Exception.Response.StatusCode.Value__
        Write-Host "[$desc] FAILED - Status: $status"
    }
}

Write-Host "=== Basedata POST API Test ==="

Test-Post "/banks" '{"code":"BANK99","name":"TestBank","status":"1"}' "Bank"
Test-Post "/business-units" '{"code":"BU99","name":"TestUnit","status":"1"}' "BusinessUnit"
Test-Post "/traders" '{"code":"T99","name":"Test","status":"1"}' "Trader"
Test-Post "/countries" '{"code":"YY","name":"Test","status":"1"}' "Country"
Test-Post "/counterparties" '{"code":"CP99","name":"TestCP","status":"1"}' "Counterparty"
Test-Post "/currencies" '{"code":"TST","name":"Test","status":"1"}' "Currency"
Test-Post "/counterparty-accounts" '{"accountNo":"ACC99","counterpartyId":1,"bankId":1,"currency":"USD","status":"1"}' "CounterpartyAccount"
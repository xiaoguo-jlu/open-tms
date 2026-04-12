$baseUrl = "http://localhost:8081/api/v1"
$timestamp = Get-Date -Format "HHmmss"

Write-Host "=== Basedata POST API Test ==="

$tests = @(
    @{path="/banks"; data='{"code":"BANK' + $timestamp + '","name":"TestBank","status":"1"}'; desc="Bank"}
    @{path="/business-units"; data='{"code":"BU' + $timestamp + '","name":"TestUnit","status":"1"}'; desc="BusinessUnit"}
    @{path="/traders"; data='{"code":"T' + $timestamp + '","name":"Test","status":"1"}'; desc="Trader"}
    @{path="/countries"; data='{"code":"C' + $timestamp + '","name":"Test","status":"1"}'; desc="Country"}
    @{path="/counterparties"; data='{"code":"CP' + $timestamp + '","name":"TestCP","status":"1"}'; desc="Counterparty"}
    @{path="/currencies"; data='{"code":"TST' + $timestamp + '","name":"Test","status":"1"}'; desc="Currency"}
    @{path="/counterparty-accounts"; data='{"accountNo":"ACC' + $timestamp + '","counterpartyId":5,"bankId":5,"currency":"USD","status":"1"}'; desc="CounterpartyAccount"}
)

$passed = 0
foreach ($t in $tests) {
    try {
        $r = Invoke-WebRequest -Uri "$baseUrl$($t.path)" -Method POST -ContentType "application/json" -Body $t.data -UseBasicParsing -ErrorAction Stop
        Write-Host "[OK] $($t.desc) - $($t.path) : $($r.StatusCode)"
        $passed++
    } catch {
        Write-Host "[FAIL] $($t.desc) - $($t.path) : $($_.Exception.Response.StatusCode.Value__)"
    }
}

Write-Host "`n=== Summary: $passed/$($tests.Count) passed ==="
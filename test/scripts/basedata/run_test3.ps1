$ErrorActionPreference = "Continue"
$output = & python "F:\code\opencode\opentrm\test\scripts\basedata\test_all_post.py" 2>&1
if ($output) { Write-Host $output } else { Write-Host "No output" }

# Try with full path
Write-Host "Trying with full python path..."
$output2 = & "C:\Users\qingmu\AppData\Local\Microsoft\WindowsApps\python.exe" "F:\code\opencode\opentrm\test\scripts\basedata\test_all_post.py" 2>&1
Write-Host $output2
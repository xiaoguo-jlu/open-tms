$ErrorActionPreference = "Continue"
$output = & "python" "F:\code\opencode\opentrm\test\scripts\basedata\test_all_post.py" 2>&1
Write-Host $output
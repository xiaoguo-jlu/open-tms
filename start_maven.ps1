$env:JAVA_HOME = "D:\Program Files\Java\jdk-21.0.11"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$proc = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "cd /d E:\code-project\open-tms\open-tms && mvn spring-boot:run -pl basedata -DskipTests" -PassThru -NoNewWindow
Write-Host "Started Maven PID: $($proc.Id)"
Start-Sleep 5
$output = ""
while (!$proc.HasExited) {
    Start-Sleep 2
    Write-Host "Still running..."
}
Write-Host "Maven process exited with code: $($proc.ExitCode)"
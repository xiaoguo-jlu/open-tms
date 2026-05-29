import subprocess
import time
import os
import urllib.request

# Kill any existing process on port 8081
try:
    result = subprocess.run(['netstat', '-ano'], capture_output=True, text=True)
    for line in result.stdout.splitlines():
        if ':8081' in line and 'LISTENING' in line:
            parts = line.split()
            if parts and parts[-1].isdigit():
                pid = int(parts[-1])
                try:
                    subprocess.run(['taskkill', '/F', '/PID', str(pid)], capture_output=True)
                    print(f'Killed process {pid}')
                except:
                    pass
except:
    pass

time.sleep(2)

# Start the backend
jar_path = 'E:\\code-project\\open-tms\\open-tms\\basedata\\target\\opentms-basedata-1.0.0-SNAPSHOT.jar'
proc = subprocess.Popen(['java', '-jar', jar_path], stdout=subprocess.PIPE, stderr=subprocess.PIPE)
print(f'Started process {proc.pid}')

# Wait for startup
time.sleep(25)

# Test endpoint
try:
    resp = urllib.request.urlopen('http://localhost:8081/opentms/basedata/api/v1/countries/page?pageNum=1&pageSize=10', timeout=10)
    print(f'Status: {resp.status}')
    print(f'Response: {resp.read().decode("utf-8")[:500]}')
except urllib.error.HTTPError as e:
    print(f'HTTP Error: {e.code}')
    print(f'Response: {e.read().decode("utf-8")[:500]}')
except Exception as e:
    print(f'Error: {e}')
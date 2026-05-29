import subprocess
import time
import urllib.request
import json

jar_path = 'E:\\code-project\\open-tms\\open-tms\\basedata\\target\\opentms-basedata-1.0.0-SNAPSHOT.jar'

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

time.sleep(5)

# Start the backend
print('Starting backend...')
proc = subprocess.Popen(['java', '-jar', jar_path], stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)

# Wait for startup
started = False
for line in proc.stdout:
    print(line.strip())
    if 'Started BasedataApplication' in line:
        started = True
        print('Backend started!')
        break

if started:
    print('Waiting 5 seconds before testing...')
    time.sleep(5)

    # Test endpoints
    endpoints = [
        'http://localhost:8081/opentms/basedata/api/v1/countries',
        'http://localhost:8081/opentms/basedata/api/v1/countries/page?pageNum=1&pageSize=10',
        'http://localhost:8081/opentms/basedata/api/v1/traders',
        'http://localhost:8081/opentms/basedata/api/v1/holidays',
    ]

    for url in endpoints:
        print(f'\nTesting: {url}')
        try:
            req = urllib.request.Request(url)
            req.add_header('Accept', 'application/json')
            resp = urllib.request.urlopen(req, timeout=10)
            print(f'  Status: {resp.status}')
            data = resp.read().decode('utf-8')[:500]
            print(f'  Response: {data}')
        except urllib.error.HTTPError as e:
            print(f'  HTTP Error: {e.code}')
            body = e.read().decode('utf-8')[:500]
            print(f'  Body: {body}')
        except Exception as e:
            print(f'  Error: {e}')
else:
    print('Backend failed to start')
    proc.terminate()
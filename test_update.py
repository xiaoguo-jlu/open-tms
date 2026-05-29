import subprocess
import time
import requests
import io
import sys

# Fix encoding for Windows
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

print("Starting backend...")
proc = subprocess.Popen(
    ["java", "-jar", "basedata/target/opentms-basedata-1.0.0-SNAPSHOT.jar"],
    stdout=subprocess.PIPE,
    stderr=subprocess.STDOUT,
    text=True,
    bufsize=1,
    cwd=r"E:\code-project\open-tms\open-tms"
)

print("Waiting for backend to start...")
time.sleep(20)

print("\nTesting update...")
body = {
    'id': 1,
    'code': 'CNY',
    'name': 'Chinese Yuan',
    'symbol': 'Y',
    'decimalPlaces': 2,
    'status': '1'
}
resp = requests.post('http://localhost:8081/opentms/basedata/api/v1/currencies/update', json=body, timeout=10)
print('Status:', resp.status_code)
print('Response:', resp.text)

print("\nStopping backend...")
proc.terminate()

print("\nBackend log:")
try:
    lines = proc.stdout.readlines()
    for line in lines:
        try:
            line = line.encode('utf-8', errors='replace').decode('utf-8')
        except:
            line = str(line)
        if '[更新币种]' in line or 'MyBatis' in line or 'ERROR' in line or 'Exception' in line:
            print(line, end='')
except Exception as e:
    print("Error reading log:", e)
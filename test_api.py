import requests
import json

base_url = "http://localhost:8081/api/v1"

# Test Bank POST
print("=== Test POST /api/v1/banks ===")
response = requests.post(
    f"{base_url}/banks",
    json={"code": "TEST001", "name": "测试银行", "status": "1"},
    headers={"Content-Type": "application/json"}
)
print(f"Status: {response.status_code}")
print(f"Response: {response.text[:500] if response.text else 'empty'}")

# Test Counterparty POST
print("\n=== Test POST /api/v1/counterparties ===")
response = requests.post(
    f"{base_url}/counterparties",
    json={"code": "CP001", "name": "测试对手", "status": "1"},
    headers={"Content-Type": "application/json"}
)
print(f"Status: {response.status_code}")
print(f"Response: {response.text[:500] if response.text else 'empty'}")

# Test BusinessUnit POST
print("\n=== Test POST /api/v1/business-units ===")
response = requests.post(
    f"{base_url}/business-units",
    json={"code": "BU001", "name": "测试单元", "status": "1"},
    headers={"Content-Type": "application/json"}
)
print(f"Status: {response.status_code}")
print(f"Response: {response.text[:500] if response.text else 'empty'}")
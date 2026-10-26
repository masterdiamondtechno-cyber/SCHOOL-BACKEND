#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
=============================================================
  SCHOOL MANAGEMENT SaaS - Complete API Test Suite
  Server: http://localhost:8080
=============================================================
  HOW TO RUN:
    Step 1: Make sure Spring Boot server is running (port 8080)
    Step 2: Open PowerShell / CMD (NOT inside AI sandbox)
    Step 3: python api_test_suite.py
=============================================================
"""

import json, sys, time, uuid
from datetime import datetime

try:
    import urllib.request, urllib.error
except ImportError:
    print("ERROR: Python 3 required"); sys.exit(1)

# ─────────────────────────── CONFIG ───────────────────────
BASE_URL   = "http://localhost:8080"
DB_URL     = "postgresql://postgres:root@localhost:5432/school"  # info only

# A unique test school so no conflict with existing data
UNIQUE_ID  = str(uuid.uuid4())[:8].upper()
TEST_EMAIL = f"testschool_{UNIQUE_ID}@apitest.com"
TEST_PHONE = "9000000001"
TEST_PASS  = "Test@12345"

results = []

# ─────────────────────────── HELPERS ──────────────────────
def make_request(method, path, body=None, token=None):
    url     = BASE_URL + path
    headers = {"Content-Type": "application/json", "Accept": "application/json"}
    if token:
        headers["Authorization"] = "Bearer " + token
    data = json.dumps(body).encode("utf-8") if body else None
    req  = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=15) as r:
            raw = r.read()
            try:    return r.getcode(), json.loads(raw), None
            except: return r.getcode(), raw.decode("utf-8","replace"), None
    except urllib.error.HTTPError as e:
        raw = e.read()
        try:    return e.code, json.loads(raw), str(e)
        except: return e.code, raw.decode("utf-8","replace"), str(e)
    except Exception as ex:
        return 0, None, str(ex)

def log(name, status, resp, err, expected):
    passed = status in expected
    icon   = "[PASS]" if passed else "[FAIL]"
    results.append({"name": name, "passed": passed, "status": status})
    bar = "-" * 55
    print(f"\n{bar}")
    print(f"  {icon}  {name}  [HTTP {status}]")
    if not passed and err:
        print(f"  Error : {err}")
    if isinstance(resp, dict):
        s = json.dumps(resp, indent=2, ensure_ascii=False)
        print(s[:600] + ("\n  ...(truncated)" if len(s) > 600 else ""))
    elif resp:
        print(str(resp)[:400])
    print(bar)
    return passed

def section(title):
    print(f"\n{'='*55}")
    print(f"  {title}")
    print(f"{'='*55}")

# ═══════════════════════════════════════════════════════════
#   STEP 0: Health Check
# ═══════════════════════════════════════════════════════════
section("STEP 0 | Health Check")
status, resp, err = make_request("GET", "/actuator/health")
ok = log("Server Health", status, resp, err, [200])
if not ok:
    print("\n[FATAL] Server not running on port 8080. Start Spring Boot first!")
    sys.exit(1)

# ═══════════════════════════════════════════════════════════
#   STEP 1: Public — All Subscription Plans
# ═══════════════════════════════════════════════════════════
section("STEP 1 | Fetch All Subscription Plans (public)")
status, resp, err = make_request("GET", "/api/subscription/plans")
log("GET /api/subscription/plans", status, resp, err, [200])

# ═══════════════════════════════════════════════════════════
#   STEP 2: Public — Individual Plan Details
# ═══════════════════════════════════════════════════════════
section("STEP 2 | Individual Plan Details (public)")
for plan in ["BASIC", "PRO", "ENTERPRISE"]:
    status, resp, err = make_request("GET", f"/api/subscription/plans/{plan}")
    log(f"GET /api/subscription/plans/{plan}", status, resp, err, [200])

# ═══════════════════════════════════════════════════════════
#   STEP 3: Register a new Test School
# ═══════════════════════════════════════════════════════════
section(f"STEP 3 | Register New Test School ({TEST_EMAIL})")

import urllib.parse
# School registration uses multipart/form-data
boundary = "----FormBoundary" + UNIQUE_ID
form_data = {
    "schoolName":    f"API Test School {UNIQUE_ID}",
    "email":          TEST_EMAIL,
    "phoneNo":        TEST_PHONE,
    "udiseNo":        f"UD{UNIQUE_ID}",
    "state":          "Maharashtra",
    "district":       "Pune",
    "taluka":         "Haveli",
    "pincode":        "411001",
    "schoolAddress":  "Test Address, API Testing",
    "principalName":  "Test Principal",
}
body_parts = []
for key, val in form_data.items():
    body_parts.append(f"--{boundary}\r\nContent-Disposition: form-data; name=\"{key}\"\r\n\r\n{val}")
body_parts.append(f"--{boundary}--")
form_body = "\r\n".join(body_parts).encode("utf-8")

reg_headers = {
    "Content-Type": f"multipart/form-data; boundary={boundary}",
    "Accept": "application/json"
}
req = urllib.request.Request(BASE_URL + "/api/AddSchool/create",
                              data=form_body, headers=reg_headers, method="POST")
try:
    with urllib.request.urlopen(req, timeout=15) as r:
        reg_status, reg_resp = r.getcode(), json.loads(r.read())
except urllib.error.HTTPError as e:
    reg_status = e.code
    try:    reg_resp = json.loads(e.read())
    except: reg_resp = {}
except Exception as ex:
    reg_status, reg_resp = 0, {"error": str(ex)}

log("POST /api/AddSchool/create", reg_status, reg_resp, None, [200, 201])

SCHOOL_ID = None
if isinstance(reg_resp, dict):
    data = reg_resp.get("data") or {}
    if isinstance(data, dict):
        SCHOOL_ID = data.get("schoolId") or data.get("id")

if not SCHOOL_ID:
    print("\n  [!] Could not get schoolId from registration response.")
    print("      Trying to find it via Swagger or use existing school ID=3")
    SCHOOL_ID = 3
else:
    print(f"\n  [+] New School registered! schoolId = {SCHOOL_ID}")

# ═══════════════════════════════════════════════════════════
#   STEP 4: Login with Test School
# ═══════════════════════════════════════════════════════════
section("STEP 4 | Login to get JWT Token")
print(f"  Trying email: {TEST_EMAIL}")
print(f"  NOTE: If newly registered school needs approval first,")
print(f"        login may fail. In that case, try existing account.")

login_body = {"username": TEST_EMAIL, "password": TEST_PASS, "role": "SCHOOL_ADMIN"}
status, resp, err = make_request("POST", "/api/AddSchool/login", body=login_body)

# Try alternate login endpoint
if status not in [200, 201]:
    print(f"\n  [!] /api/AddSchool/login gave {status}, trying /api/auth/login ...")
    status, resp, err = make_request("POST", "/api/auth/login", body=login_body)

log("Login (JWT)", status, resp, err, [200, 201])

JWT_TOKEN = None
if status in [200, 201] and isinstance(resp, dict):
    data = resp.get("data") or resp
    if isinstance(data, dict):
        JWT_TOKEN = (data.get("token") or data.get("accessToken")
                     or data.get("access_token") or data.get("jwt"))
    if not JWT_TOKEN:
        JWT_TOKEN = resp.get("token") or resp.get("accessToken")

if JWT_TOKEN:
    print(f"\n  [+] JWT obtained! (first 60 chars): {JWT_TOKEN[:60]}...")
    # Try to get schoolId from JWT payload
    try:
        import base64
        payload_b64 = JWT_TOKEN.split(".")[1]
        payload_b64 += "=" * (4 - len(payload_b64) % 4)
        payload = json.loads(base64.b64decode(payload_b64))
        print(f"  [+] JWT Claims: {json.dumps(payload, indent=6)}")
        if payload.get("sid"):
            SCHOOL_ID = payload.get("sid")
            print(f"  [+] schoolId from JWT: {SCHOOL_ID}")
    except Exception as e:
        print(f"  [!] Could not decode JWT: {e}")
else:
    print(f"\n  [!] Login FAILED. School may need admin approval.")
    print(f"      Run this SQL in pgAdmin/psql to approve:")
    print(f"      UPDATE school SET registration_status='APPROVED', school_status='ACTIVE'")
    print(f"      WHERE email='{TEST_EMAIL}';")
    print(f"\n  Continuing tests with schoolId={SCHOOL_ID} (no auth)...")

# ═══════════════════════════════════════════════════════════
#   STEP 5: Check Subscription Status
# ═══════════════════════════════════════════════════════════
section(f"STEP 5 | Check Subscription Status (schoolId={SCHOOL_ID})")
status, resp, err = make_request("GET", f"/api/subscription/check/{SCHOOL_ID}", token=JWT_TOKEN)
log(f"GET /api/subscription/check/{SCHOOL_ID}", status, resp, err, [200, 201, 404])

# ═══════════════════════════════════════════════════════════
#   STEP 6: Payment History
# ═══════════════════════════════════════════════════════════
section(f"STEP 6 | Payment History (schoolId={SCHOOL_ID})")
status, resp, err = make_request("GET", f"/api/subscription/payments/{SCHOOL_ID}", token=JWT_TOKEN)
log(f"GET /api/subscription/payments/{SCHOOL_ID}", status, resp, err, [200])

# ═══════════════════════════════════════════════════════════
#   STEP 7: Create Razorpay Order (Mock mode)
# ═══════════════════════════════════════════════════════════
section(f"STEP 7 | Create Razorpay Order (schoolId={SCHOOL_ID})")
order_body = {"planId": 2, "billingCycle": "MONTHLY"}
status, resp, err = make_request("POST", f"/api/subscription/create-order/{SCHOOL_ID}",
                                  body=order_body, token=JWT_TOKEN)
log(f"POST /api/subscription/create-order/{SCHOOL_ID}", status, resp, err, [200, 201])

ORDER_ID = None
if isinstance(resp, dict):
    d = resp.get("data") or resp
    if isinstance(d, dict):
        ORDER_ID = d.get("razorpayOrderId") or d.get("orderId") or d.get("id")
print(f"\n  Razorpay Order ID: {ORDER_ID}")

# ═══════════════════════════════════════════════════════════
#   STEP 8: Verify Payment (Mock mode)
# ═══════════════════════════════════════════════════════════
section(f"STEP 8 | Verify Payment / Mock (schoolId={SCHOOL_ID})")
verify_body = {
    "razorpayOrderId":   ORDER_ID or "order_mock_APITEST123",
    "razorpayPaymentId": "pay_mock_APITEST456",
    "razorpaySignature": "mock_signature_APITEST",
    "planId":            2,
    "billingCycle":      "MONTHLY",
    "schoolId":          SCHOOL_ID
}
status, resp, err = make_request("POST", f"/api/subscription/verify-payment/{SCHOOL_ID}",
                                  body=verify_body, token=JWT_TOKEN)
log(f"POST /api/subscription/verify-payment/{SCHOOL_ID}", status, resp, err, [200, 201, 400])

# ═══════════════════════════════════════════════════════════
#   STEP 9: Re-check Subscription after payment
# ═══════════════════════════════════════════════════════════
section(f"STEP 9 | Re-check Subscription after Payment")
status, resp, err = make_request("GET", f"/api/subscription/check/{SCHOOL_ID}", token=JWT_TOKEN)
log(f"GET /api/subscription/check/{SCHOOL_ID} (after payment)", status, resp, err, [200])

# ═══════════════════════════════════════════════════════════
#   STEP 10: Interceptor Test — No-auth on protected route
# ═══════════════════════════════════════════════════════════
section("STEP 10 | Security — Protected route without token")
status, resp, err = make_request("GET", f"/api/students/all/{SCHOOL_ID}")
log("GET /api/students (no auth) -> expect 401/403", status, resp, err, [401, 403])

# ═══════════════════════════════════════════════════════════
#   STEP 11: Swagger UI
# ═══════════════════════════════════════════════════════════
section("STEP 11 | Swagger UI")
status, resp, err = make_request("GET", "/swagger-ui/index.html")
log("GET /swagger-ui/index.html", status, resp, err, [200])

# ═══════════════════════════════════════════════════════════
#   FINAL REPORT
# ═══════════════════════════════════════════════════════════
total  = len(results)
passed = sum(1 for r in results if r["passed"])
failed = total - passed

print(f"\n\n{'#'*55}")
print(f"  FINAL TEST REPORT  |  {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
print(f"{'#'*55}")
for r in results:
    icon = "[PASS]" if r["passed"] else "[FAIL]"
    print(f"  {icon}  [{r['status']:>3}]  {r['name']}")
print(f"{'='*55}")
print(f"  Total: {total}   Passed: {passed}   Failed: {failed}")
print(f"{'#'*55}")

if failed == 0:
    print("\n  ALL TESTS PASSED! Ready to push to GitHub.\n")
else:
    print(f"\n  {failed} FAILED. Common fixes:")
    print("  - School needs approval: run SQL UPDATE in pgAdmin")
    print("  - Wrong password: update TEST_PASS in this script")
    print("  - Server not on port 8080: update BASE_URL\n")

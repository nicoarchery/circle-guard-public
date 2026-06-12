import os
import random
from uuid import uuid4

from locust import HttpUser, task, between

AUTH_URL = os.getenv("CIRCLEGUARD_AUTH_URL", "http://localhost:8180")
GATEWAY_URL = os.getenv("CIRCLEGUARD_GATEWAY_URL", "http://localhost:8087")
FORM_URL = os.getenv("CIRCLEGUARD_FORM_URL", "http://localhost:8086")
PROMOTION_URL = os.getenv("CIRCLEGUARD_PROMOTION_URL", "http://localhost:8088")


class CircleGuardUser(HttpUser):
    wait_time = between(1, 3)

    @task(3)
    def visitor_flow(self):
        anonymous_id = str(uuid4())
        handoff = self.client.post(
            f"{AUTH_URL}/api/v1/auth/visitor/handoff",
            json={"anonymousId": anonymous_id},
            name="auth_visitor_handoff",
        )
        if handoff.status_code != 200:
            return

        token = handoff.json().get("token")
        if not token:
            return

        self.client.post(
            f"{GATEWAY_URL}/api/v1/gate/validate",
            json={"token": token},
            name="gateway_validate",
        )

        survey_payload = {
            "anonymousId": anonymous_id,
            "hasFever": random.choice([True, False]),
            "hasCough": random.choice([True, False]),
            "otherSymptoms": "",
        }
        self.client.post(
            f"{FORM_URL}/api/v1/surveys",
            json=survey_payload,
            name="form_submit_survey",
        )

    @task(2)
    def health_center_report_flow(self):
        login = self.client.post(
            f"{AUTH_URL}/api/v1/auth/login",
            json={"username": "health_user", "password": "password"},
            name="auth_login_health",
        )
        if login.status_code != 200:
            return

        token = login.json().get("token")
        anonymous_id = login.json().get("anonymousId")
        if not token or not anonymous_id:
            return

        payload = {
            "anonymousId": anonymous_id,
            "status": random.choice(["POTENTIAL", "CONFIRMED"]),
            "adminOverride": random.choice([True, False]),
        }
        self.client.post(
            f"{PROMOTION_URL}/api/v1/health/report",
            json=payload,
            headers={"Authorization": f"Bearer {token}"},
            name="promotion_report_status",
        )

    @task(1)
    def login_smoke_flow(self):
        username = os.getenv("CIRCLEGUARD_USERNAME", "super_admin")
        password = os.getenv("CIRCLEGUARD_PASSWORD", "password")
        self.client.post(
            f"{AUTH_URL}/api/v1/auth/login",
            json={"username": username, "password": password},
            name="auth_login",
        )

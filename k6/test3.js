import http from "k6/http";
import { check, sleep } from "k6";
import exec from "k6/execution";

export const options = {
    stages: [
        { duration: "30s", target: 50 },
        { duration: "2m", target: 100 },
        { duration: "30s", target: 0 },
    ],
};

const BASE = "http://spring:8080";

const POST_ID = 3;
const USER_ID = "user-2";

// ✅ 방금 만든 범위와 맞추기
const ATTACH_START = 400001;
const ATTACH_COUNT = 10000;

function headers() {
    return { "Content-Type": "application/json", "X-User-Id": USER_ID };
}

export default function () {
    const it = exec.scenario.iterationInTest;
    const attachmentId = ATTACH_START + (it % ATTACH_COUNT);

    const res = http.post(
        `${BASE}/post/${POST_ID}/attachments`,
        JSON.stringify([attachmentId]),
        { headers: headers() }
    );

    check(res, {
        "204 or 403": (r) => r.status === 204 || r.status === 403,
        "no 500": (r) => r.status !== 500,
    });

    // 너무 빠르면 DB에 락 경합이 과도해질 수 있으니, 처음엔 아주 짧게라도
    sleep(0.01);
}
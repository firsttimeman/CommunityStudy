import http from "k6/http";
import { check, sleep } from "k6";
import { Counter } from "k6/metrics";

export const options = {
    stages: [
        { duration: "2m", target: 50 },   // 워밍업
        { duration: "3m", target: 150 },  // 증가
        { duration: "10m", target: 200 }, // 안정 구간 (관찰 핵심)
        { duration: "2m", target: 0 },    // 종료
    ],
};

const fails = new Counter("fails");
const BASE = "http://spring:8080";

function headers() {
    return {
        "Content-Type": "application/json",
        "X-User-Id": `user-${__VU}`,
    };
}

function safeJson(res) {
    try {
        return res.json();
    } catch (e) {
        return null;
    }
}

function logFail(name, res) {
    const body = res?.body ? res.body.slice(0, 300) : "";
    console.error(`[FAIL] ${name} status=${res.status} url=${res.url} body=${body}`);
    fails.add(1, { name, status: String(res.status) });
}

export default function () {
    const h = { headers: headers(), timeout: "30s" };
    const chance = Math.random();

    // 70% 상세 조회
    if (chance < 0.7) {
        const res = http.get(`${BASE}/test/hot/detail`, h);
        const body = safeJson(res);

        const ok = check(res, {
            "detail status 200": (r) => r.status === 200,
            "detail body exists": () => body !== null,
        });

        if (!ok) logFail("hot-detail", res);
    }
    // 30% 댓글 작성
    else {
        const payload = JSON.stringify({
            content: `k6-comment-${__VU}-${__ITER}`.slice(0, 100),
        });

        const res = http.post(`${BASE}/test/hot/comment`, payload, h);
        const body = safeJson(res);

        const ok = check(res, {
            "comment status 200": (r) => r.status === 200,
            "comment body exists": () => body !== null,
        });

        if (!ok) logFail("hot-comment", res);
    }

    sleep(Math.random() * 0.3);
}
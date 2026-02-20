import http from "k6/http";
import { check, sleep } from "k6";
import { Counter } from "k6/metrics";

export const options = {
    stages: [
        { duration: "2m", target: 200 },
        { duration: "3m", target: 500 },
        { duration: "5m", target: 800 },
    ],
};

const fails = new Counter("fails");
const BASE = "http://spring:8080";

const TOTAL_POSTS = 50000;
const SIZE = 50;

function headers() {
    return {
        "Content-Type": "application/json",
        "X-User-Id": `user-${__VU}`,
    };
}

function logFail(name, res) {
    const body = res?.body ? res.body.slice(0, 300) : "";
    console.error(`[FAIL] ${name} status=${res.status} url=${res.url} body=${body}`);
    fails.add(1, { name, status: String(res.status) });
}

function pickPage() {
    const maxPage = Math.max(1, Math.floor(TOTAL_POSTS / SIZE));
    return Math.floor(Math.random() * maxPage);
}

function pickPostId() {
    // 1 ~ TOTAL_POSTS 사이에서 랜덤
    return 1 + Math.floor(Math.random() * TOTAL_POSTS);
}

export default function () {
    const params = { headers: headers(), timeout: "120s" };

    const r = Math.random();

    // 90%: 인기 피드
    if (r < 0.9) {
        const page = pickPage();
        const res = http.get(`${BASE}/post/popular?page=${page}&size=${SIZE}`, params);

        const ok = check(res, { "popular 200": (x) => x.status === 200 });
        if (!ok) logFail("popular-feed", res);

        // 10%: 상세 조회
    } else {
        const id = pickPostId();
        const res = http.get(`${BASE}/post/${id}`, params);

        const ok = check(res, { "detail 200": (x) => x.status === 200 || x.status === 404 });
        // 데이터가 없어서 404가 나와도 “DB 부하”는 걸리니까 테스트 목적상 허용 가능
        if (!ok) logFail("post-detail", res);
    }

    sleep(Math.random() * 0.2);
}
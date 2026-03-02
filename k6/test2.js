import http from "k6/http";
import { check, sleep } from "k6";
import { Counter } from "k6/metrics";

export const options = {
    stages: [
        { duration: "5m", target: 200 },
        { duration: "5m", target: 400 },
        { duration: "5m", target: 600 },
        { duration: "5m", target: 800 },
        { duration: "5m", target: 800 }, // 안정 구간(관측용)
    ],

    discardResponseBodies: true,

    thresholds: {
        http_req_failed: ["rate<0.01"],
        http_req_duration: ["p(95)<800"],
    },
};

const fails = new Counter("fails");
const BASE = "http://spring:8080";
const SIZE = 50;

// 댓글 1만개 달린 포스트들(heavy read)
const HEAVY_READ_IDS = [91, 81, 41, 21, 11, 31, 71, 1, 61, 51];

// write 대상 포스트들
const HOT_WRITE_IDS = [
    30, 29, 33, 26, 34, 35, 36, 38, 39, 40,
    42, 43, 44, 45, 47, 48, 49, 14, 2, 3,
    4, 5, 6, 7, 8, 9, 10, 12, 13, 28,
];

function headers() {
    return {
        "Content-Type": "application/json",
        "X-User-Id": `user-${__VU}`,
    };
}

function logFail(name, res) {
    console.error(`[FAIL] ${name} status=${res.status} url=${res.url}`);
    fails.add(1, { name, status: String(res.status) });
}

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

function pickHotPage() {
    // 인기피드 상위 20페이지 집중
    return Math.floor(Math.random() * 20);
}

function pickCommentPage() {
    // heavy read도 핫페이지 위주
    return Math.floor(Math.random() * 20);
}

function makeComment() {
    return { content: `k6-c-${__VU}-${__ITER}-${Date.now()}` };
}

export default function () {
    const params = {
        headers: headers(),
        timeout: "15s", // 현실형은 30s까지 길게 끌기보다 빠르게 실패를 드러내는 편이 관측에 좋음
    };

    const r = Math.random();

    // ✅ 현실형 분포:
    // 70%: 인기 피드(가벼운 read)
    // 10%: heavy read(댓글 조회)
    // 15%: 댓글 작성(write)
    // 5% : "기타 읽기" 자리에 여유(여기선 popular를 한번 더 섞어둠)

    if (r < 0.70) {
        // popular read
        const page = pickHotPage();
        const res = http.get(`${BASE}/post/popular?page=${page}&size=${SIZE}`, params);
        const ok = check(res, { "popular 200": (x) => x.status === 200 });
        if (!ok) logFail("popular", res);

    } else if (r < 0.80) {
        // heavy comments read
        const postId = pick(HEAVY_READ_IDS);
        const page = pickCommentPage();
        const res = http.get(
            `${BASE}/comments/posts/${postId}?page=${page}&size=${SIZE}`,
            params
        );
        const ok = check(res, { "comments 200": (x) => x.status === 200 });
        if (!ok) logFail("comments-heavy-read", res);

    } else if (r < 0.95) {
        // write
        const postId = pick(HOT_WRITE_IDS);
        const payload = JSON.stringify(makeComment());
        const res = http.post(`${BASE}/comments/posts/${postId}`, payload, params);
        const ok = check(res, { "comment create 200": (x) => x.status === 200 });
        if (!ok) logFail("comment-create", res);

    } else {
        // 기타 read (여유 구간) - popular 한 번 더
        const page = pickHotPage();
        const res = http.get(`${BASE}/post/popular?page=${page}&size=${SIZE}`, params);
        const ok = check(res, { "popular2 200": (x) => x.status === 200 });
        if (!ok) logFail("popular-2", res);
    }

    // ✅ 현실적인 think time
    sleep(0.5 + Math.random() * 1.5); // 0.5~2.0s
}
import http from "k6/http";
import { check, sleep } from "k6";
import { Counter } from "k6/metrics";

export const options = {
    stages: [
        { duration: "2m", target: 200 },
        { duration: "3m", target: 500 },
        { duration: "5m", target: 800 },
    ],

    // ✅ 큰 응답 바디 때문에 네트워크/메모리/직렬화가 터지는 걸 방지
    discardResponseBodies: true,

    // (선택) 실패율/지연시간 기준을 걸어두면 "언제부터 망가지기 시작했는지" 판단 쉬움
    thresholds: {
        http_req_failed: ["rate<0.01"],        // 실패율 1% 미만 목표
        http_req_duration: ["p(95)<800"],      // p95 800ms 목표(원하는 기준으로 조절)
    },
};

const fails = new Counter("fails");
const BASE = "http://spring:8080";
const SIZE = 50;

// 댓글 1만개 달린 포스트들
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
    // discardResponseBodies=true라서 res.body는 보통 비어있음(그게 목적)
    console.error(`[FAIL] ${name} status=${res.status} url=${res.url}`);
    fails.add(1, { name, status: String(res.status) });
}

function pick(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

function pickHotPage() {
    // 인기피드는 상위 20페이지 집중
    return Math.floor(Math.random() * 20);
}

function makeComment() {
    // content 길이 제한 있으면 여기서 맞춰줘
    return { content: `k6-c-${__VU}-${__ITER}-${Date.now()}` };
}

function pickCommentPage() {
    // ✅ heavy read도 "전체"가 아니라 0~20 페이지 중 랜덤으로만 읽게 (사실상 캐시/핫페이지 패턴)
    return Math.floor(Math.random() * 20);
}

export default function () {
    const params = {
        headers: headers(),
        timeout: "30s", // ✅ 120s는 너무 길어서 실패가 늦게 터지고 시스템이 더 망가짐
    };

    const r = Math.random();

    // 20%: 인기 피드 (가벼운 read)
    if (r < 0.2) {
        const page = pickHotPage();
        const res = http.get(`${BASE}/post/popular?page=${page}&size=${SIZE}`, params);
        const ok = check(res, { "popular 200": (x) => x.status === 200 });
        if (!ok) logFail("popular", res);

        // 20%: heavy read (댓글 조회)  ✅ 반드시 페이지네이션 추가
    } else if (r < 0.4) {
        const postId = pick(HEAVY_READ_IDS);

        // ✅ 너가 컨트롤러에 page/size 붙였다는 가정
        const page = pickCommentPage();
        const res = http.get(
            `${BASE}/comments/posts/${postId}?page=${page}&size=${SIZE}`,
            params
        );

        const ok = check(res, { "comments 200": (x) => x.status === 200 });
        if (!ok) logFail("comments-heavy-read", res);

        // 60%: write (댓글 작성)
    } else {
        const postId = pick(HOT_WRITE_IDS);
        const payload = JSON.stringify(makeComment());

        const res = http.post(`${BASE}/comments/posts/${postId}`, payload, params);
        const ok = check(res, { "comment create 200": (x) => x.status === 200 });
        if (!ok) logFail("comment-create", res);
    }

    // ✅ sleep 너무 짧으면 네트워크/서버가 “초미세 요청 폭탄”이 됨
    sleep(Math.random() * 0.2);
}
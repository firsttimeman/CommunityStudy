# Community

게시글, 댓글, 파일 첨부 기능을 제공하는 커뮤니티 백엔드 프로젝트입니다.  
단순 CRUD 구현을 넘어서 **첨부파일 정합성, 동시성 제어, 멀티 인스턴스 환경의 스케줄러 안정성, 조회 성능 개선**을 고려하여 설계했습니다.

---

## Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- MySQL
- Redis
- AWS S3
- ShedLock
- JUnit5
- k6
- Grafana / Prometheus
- Docker

---

## 주요 기능

- 게시글 CRUD
- 댓글 CRUD
- 파일 업로드 및 게시글 첨부
- 만료된 첨부파일 자동 정리 스케줄러
- 댓글 동시성 제어
- 조회 API 성능 개선

---

## 문제 해결

### 1. 첨부파일 정합성 문제

게시글 작성 중 이탈할 경우 업로드된 파일이 S3에 고아 데이터로 남을 수 있었습니다.

해결

- 파일 업로드와 게시글 생성을 분리
- 첨부파일 상태를 `TEMP → ATTACHED`로 관리
- 만료된 `TEMP` 파일을 스케줄러로 정리

---

### 2. 첨부파일 동시성 문제

동일 첨부파일을 여러 요청이 동시에 게시글에 연결하려는 문제 발생

해결

- DB 조건부 UPDATE 기반 원자적 처리
- `TEMP` 상태인 경우에만 `ATTACHED`로 변경

---

### 3. 댓글 동시성 문제

동일 게시글에 댓글 요청이 집중될 경우 경쟁 상태 발생

해결

- Redis 분산 락 적용
- 게시글 단위 락 키 설계

---

### 4. 스케줄러 중복 실행 문제

멀티 인스턴스 환경에서 스케줄러가 동시에 실행될 가능성 존재

해결

- ShedLock 적용
- 하나의 인스턴스만 작업 수행

---

### 5. 조회 성능 개선

k6 기반 부하 테스트에서 조회 API의 p95 latency 증가 확인

해결

조회 패턴 기반 복합 인덱스 설계

- 최신순 → `(create_time, post_id)`
- 인기순 → `(comment_count, post_id)`
- 댓글 → `(post_id, create_time)`

결과

- p95 latency 약 **30~40% 감소**
- 평균 응답 시간 약 **7ms 수준 유지**

---

## 아키텍처

Client  
↓  
Spring Boot  
├─ MySQL  
├─ Redis  
└─ AWS S3

---

## 실행 방법

git clone <repository-url>  
cd Community  
./gradlew bootRun

---

## 배운 점

이 프로젝트를 통해 다음과 같은 부분을 경험했습니다.

- 데이터 정합성을 고려한 파일 관리 구조 설계
- Redis 분산 락을 활용한 동시성 제어
- 멀티 인스턴스 환경에서의 스케줄러 제어
- 부하 테스트 기반 성능 분석 및 인덱스 최적화
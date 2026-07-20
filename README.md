# 🤝 With Coworkers (익명 동료 평가 시스템)
> **한 달 주기의 가벼운 다면 평가를 통해 팀원들의 빠른 피드백 루프와 성장을 돕는 플랫폼**

- **개발 기간:** 2026.05 ~ 2026.07 (1인 개발)
- **서비스 링크:** [With Coworkers 바로가기](https://withcoworker.vercel.app)
- **테스트 계정:** ID: ksg / PW: gogo11##

---

## 🚀 프로젝트 개요 (Overview)
기존의 연간 동료 평가는 피드백 주기가 길어 실질적인 성장을 돕기 어려웠습니다. 
`With Coworkers`는 매달 6가지 객관식 역량 지표(의사소통, 지식공유, 적극성, 문제해결, 일정준수, 정확성)와 주관식 코멘트를 통해 일상적인 피드백을 주고받을 수 있는 비공개 팀 공간을 제공합니다.

---

## 🛠 기술 스택 (Tech Stack)
- **Backend:** Java 21, Spring Boot, Spring Data JPA
- **Frontend:** React, Vercel
- **Database & Infra:** Supabase (PostgreSQL), Redis (Docker), AWS EC2, AWS SQS, AWS SES
- **CI/CD:** GitHub Actions

---

## 🏗 시스템 아키텍처 (Architecture)
![System Architecture](./images/architecture.png)

### 💡 주요 아키텍처 설계 특징
1. **인프라 격리 및 자원 효율성:** 단일 EC2 인프라 내에서 Docker를 활용하여 웹 서버(Nginx), 애플리케이션(Spring Boot), 인메모리 캐시(Redis)를 독립된 컨테이너로 격리하여 비용을 최적화하고 환경 일관성을 확보했습니다.
2. **리버스 프록시 적용:** Nginx를 앞단에 배치하여 외부 요청을 안전하게 수신하고 내부 WAS 컨테이너로 라우팅하는 구조를 구축했습니다.
3. **비동기 이벤트 기반 알림 인프라:** 매달 진행되는 자동 리마인드 알림 발송 시 시스템 병목을 방지하기 위해 AWS SQS와 SES를 연동하여 비동기 메시징 기반의 안정적인 메일 발송 시스템을 구현했습니다.

---

## 🔥 핵심 기능 및 구현 내용 (Core Features)

### 📊 내 평가 결과 대시보드 및 데이터 시각화
- 종합 평균 및 핵심 지표(강점/약점) 메트릭 스캔 기능 구현.
- 대용량 피드백 데이터를 분기/월별 점수 추이 차트 및 방사형(Radar) 그래프로 시각화하여 시간에 따른 변화를 직관적으로 분석 가능.
- 동료들의 익명 코멘트를 안전하게 보호하고 아코디언 UI로 가독성 높게 제공.

### ⏰ 월간 자동 알림 및 리마인더 (AWS SQS + SES)
- 평가 마감 주기에 맞춰 (마감 7일 전, 2일 전, 당일 등) 스케줄러를 통한 자동 리마인드 이메일 발송.
- 대량 이메일 발송 시 사용자 요청 스레드가 차단되지 않도록 **AWS SQS큐**를 거쳐 **AWS SES**로 비동기 처리하여 백엔드 애플리케이션 가용성 확보.

### 🔐 폐쇄형 팀 공간 분리
- 조직 외부인의 접근을 차단하고, 동일 소속 팀원들 간의 다면 평가만 매핑되도록 멀티 테넌시 형태의 도메인/팀 데이터 격리 구현.

---

## 🧠 트러블 슈팅 (Troubleshooting & Architecture Decisions)

### 1️⃣ SPA(Single Page Application) 배포 시 서브 경로 라우팅 404 에러 해결
* **상황(Situation):** React로 개발한 프론트엔드를 Vercel 플랫폼에 배포한 후, `/login` 등의 특정 서브 경로로 브라우저 주소창에 직접 입력하여 접속할 때 **404 Not Found** 에러가 발생했습니다.
* **원인(Problem):** React는 Single Page Application(SPA) 구조로 클라이언트 사이드 라우팅을 수행합니다. 하지만 Vercel은 정적 호스팅 서버이기 때문에 사용자가 `/login`을 직접 입력하면 서버에서 해당 이름의 물리적 파일이나 폴더를 찾으려고 시도하여 "존재하지 않는 파일"로 오인하는 것이 원인이었습니다.
* **해결(Action):** Vercel 설정을 제어하는 `vercel.json` 파일을 프론트엔드 루트 폴더에 추가하고, 모든 경로의 요청(`/(.*)`)을 루트 인덱스 파일(`/index.html`)로 리다이렉트(`rewrites`)하도록 규칙을 정의했습니다.
  ```json
  {
    "rewrites": [
      { "source": "/(.*)", "destination": "/index.html" }
    ]
  }
  ```
* **결과(Result):** 사용자가 어떤 경로로 직접 접속하더라도 서버가 일단 `index.html`을 안전하게 반환하도록 보장했으며, 그 직후 브라우저 내의 React Router가 URL을 정상적으로 인식하여 로그인 화면을 매끄럽게 그려내도록 해결했습니다.

---

### 2️⃣ Mixed Content 및 CORS 차단을 해결하기 위한 Vercel Reverse Proxy 구축
* **상황(Situation):** HTTPS 보안 프로토콜이 적용된 Vercel 프론트엔드에서 HTTP 환경인 AWS EC2 백엔드 API 서버로 로그인 및 데이터 요청을 보낼 때, **405 Method Not Allowed** 및 **CORS 보안 제한 에러**가 발생했습니다.
* **원인(Problem):** 
  1. 현대 웹 브라우저는 보안 상 **Mixed Content(HTTPS 사이트에서 HTTP API를 호출하는 행위)**를 엄격히 차단합니다.
  2. 프론트엔드와 백엔드의 도메인이 완전히 달라 브라우저의 **CORS(Cross-Origin Resource Sharing) 제한**에 걸렸습니다.
  3. 인프라 측면에서 EC2 서버가 재부팅될 때마다 유동 IP가 계속 변경되어 통신 엔드포인트가 깨지는 문제가 복합적으로 존재했습니다.
* **해결(Action):** 
  1. **AWS 탄력적 IP(Elastic IP) 할당:** EC2 인스턴스에 고정 IPv4 주소를 매핑하여 인프라의 주소 안정성을 확보했습니다.
  2. **Vercel Reverse Proxy 설정:** 백엔드에 직접 SSL을 적용하기 전 임시 우회책으로, 브라우저가 EC2와 직접 통신하지 않고 **Vercel 호스팅 서버가 백엔드와 대신 HTTP 통신을 수행**하도록 `vercel.json`에 리버스 프록시 라우팅을 구현했습니다.
  ```json
  {
    "rewrites": [
      { "source": "/api/:path*", "destination": "http://<고정_EC2_IP>:8080/api/:path*" },
      { "source": "/oauth2/:path*", "destination": "http://<고정_EC2_IP>:8080/oauth2/:path*" },
      { "source": "/login/oauth2/:path*", "destination": "http://<고정_EC2_IP>:8080/login/oauth2/:path*" },
      { "source": "/logout", "destination": "http://<고정_EC2_IP>:8080/logout" },
      { "source": "/(.*)", "destination": "/index.html" }
    ]
  }
  ```
* **결과(Result):** 
  * 브라우저와 Vercel 구간은 자물쇠가 채워진 안전한 HTTPS 통신을 유지하고, 서버 대 서버 영역에서 HTTP 연동을 처리하여 Mixed Content 문제를 우회 해결했습니다.
  * 프론트엔드와 백엔드가 외견상 **동일한 도메인 아래에서 동작하는 것처럼 통일감**을 주어 별도의 CORS 설정 부담을 줄였으며, 실제 백엔드 EC2 주소를 클라이언트 환경에 노출하지 않는 보안 이점까지 확보했습니다.

---

### 3️⃣ GitHub Actions 비대화형(Non-interactive) 모드에 따른 환경변수 주입 실패 해결
* **상황(Situation):** GitHub Actions 워크풀로우를 통해 운영 서버 자동 배포(`docker-compose up`)를 진행했을 때, 백엔드 컨테이너가 시스템의 필수 환경 변수들을 읽어오지 못해 구동 에러가 발생했습니다.
* **원인(Problem):** GitHub Actions 러너가 SSH를 통해 배포 서버에 접속할 때는 **비대화형(Non-interactive) 및 비로그인(Non-login) 쉘 모드**로 명령을 실행합니다. 이 때문에 운영 서버의 로컬 환경 변수가 등록된 `~/.bashrc` 파일이 자동으로 로드되지 않아 변수들을 식별할 수 없었던 것이 원인이었습니다.
* **해결(Action):** 외부 실행 환경에 의존하지 않는 안전한 배포를 위해, Docker Compose의 내장 메커니즘을 활용하기로 결정했습니다. EC2 백엔드 프로젝트 루트 경로에 **`.env` 파일**을 직접 생성하고 실행에 필요한 환경 변수들을 명시해 두었습니다.
* **결과(Result):** `docker-compose` 명령어가 구동될 때 현재 폴더 내부의 `.env` 파일을 자동으로 검색하여 변수를 주입하는 규칙 덕분에, GitHub Actions의 실행 모드와 관계없이 컨테이너 내부로 필요한 환경 변수들이 안전하고 일관되게 주입되도록 자동화 인프라를 보완했습니다.

---
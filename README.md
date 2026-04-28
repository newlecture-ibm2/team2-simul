# Simul

> **입어보고, 모으고, 공유하다**
> AI 가상시착 결과를 중심으로 개인 옷장을 구성하고 커뮤니티에 공유하는 패션 소셜 플랫폼

본 리포지토리는 "Simul" 프로젝트의 MVP 기획 및 기술 명세 문서를 관리하는 공간입니다. 패션에 관심 많은 일반 소비자가 실제 구매 전 AI로 옷을 시각적으로 경험하고, 그 결과를 커뮤니티에 공유하며 개인 옷장에 모아둘 수 있는 서비스를 지향합니다.

## 시스템 명세 문서록

아래의 문서들을 통해 프로젝트의 기획 및 기술 구조를 살펴볼 수 있습니다.

- [**MVP 요구사항 기능서 (simul-mvp-spec.md)**](./docs/simul-mvp-spec.md)
  - 서비스 개요, 타겟 유저, 그리고 AI 가상시착/개인 옷장/커뮤니티 피드 등 3대 핵심 기능의 요구사항과 제약 조건을 정의합니다.
- [**기능 명세서 (simul-functional-spec.md)**](./docs/simul-functional-spec.md)
  - 프론트엔드 및 백엔드 개발을 위한 화면별 UI 컴포넌트, 기능 ID 기반의 상세 로직, 공통 데이터 모델 및 에러 코드 등을 정의합니다.
- [**API 명세서 (simul-api-spec.md)**](./docs/simul-api-spec.md)
  - REST API 엔드포인트, Request/Response 규격, 그리고 클라이언트-서버 간 데이터 통신 포맷을 정의합니다. (SSE 스트림 포함)
- [**ERD 문서 (simul-erd.md)**](./docs/simul-erd.md)
  - Mermaid.js를 활용하여 DB 스키마 구조와 테이블 간의 연관 관계(Entity-Relationship)를 정의합니다. 고립 데이터 정책 및 자동 블라인드 정책 등 주요 설계 포인트도 포함합니다.
- [**Git 코드/협업 컨벤션**](./docs/git_convention.md)
  - 브랜치 생성 규칙, MR 전략 및 커밋 컨벤션 가이드입니다.

---

## 🛠 팀원 전용: 초기 개발 환경 세팅 (필수)

프로젝트를 새로 클론받으신 팀원 분들은, 아래 방법 중 편하신 방식을 하나 선택하여 **Gitmoji 자동화 설정**을 꼭 진행해 주세요.
적용 시, 커밋 타입(`feat:`, `fix:` 등) 입력 시 자동으로 알맞은 이모지(`✨`, `🐛`)가 삽입됩니다.

### 방법 A. 명령어 1회 실행 (가장 추천)
프로젝트 디렉토리 최상단의 터미널(Git Bash, MacOS Terminal 등)에서 아래 명령어를 실행해 주세요.
```bash
git config core.hooksPath .githooks
```

### 방법 B. `npm install` 활용법
만약 프로젝트 구성이 끝나 최상단에 `package.json`이 있는 경우, 아래처럼 `postinstall` 스크립트를 추가하면 팀원들이 `npm install` 실행 시 훅이 자동으로 적용됩니다.
```json
"scripts": {
  "postinstall": "git config core.hooksPath .githooks"
}
```

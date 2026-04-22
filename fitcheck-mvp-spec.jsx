import { useState } from "react";

const sections = [
  {
    id: "overview",
    label: "서비스 개요",
    icon: "◈",
    content: {
      type: "overview",
      data: {
        name: "FitCheck",
        tagline: "입어보고, 모으고, 공유하다",
        description:
          "AI 가상시착 결과를 중심으로 개인 옷장을 구성하고 커뮤니티에 공유하는 패션 소셜 플랫폼. 패션에 관심 많은 일반 소비자가 실제 구매 전 시각적으로 옷을 경험하고, 타인의 시착 결과에서 영감을 얻어 외부 쇼핑몰로 바로 연결되는 End-to-End 패션 디스커버리 서비스.",
        target: "패션에 관심 많은 20–35세 일반 소비자",
        platform: "iOS / Android (모바일 우선)",
        phase: "MVP (핵심 4기능 중심)",
        priorities: [
          { rank: 1, name: "AI 가상시착", color: "#E8FF47" },
          { rank: 2, name: "개인 옷장 (디지털 클로젯)", color: "#B8F0C8" },
          { rank: 3, name: "커뮤니티 피드 + 공유", color: "#C8E8FF" },
          { rank: 4, name: "제품 태그 & 쇼핑 연결", color: "#F0C8E8" },
        ],
      },
    },
  },
  {
    id: "tryon",
    label: "AI 가상시착",
    icon: "①",
    badge: "P1",
    content: {
      type: "feature",
      data: {
        title: "AI 가상시착 (Virtual Try-On)",
        priority: "P1 — 핵심 차별점",
        goal: "사용자가 자신의 사진에 원하는 옷을 AI로 입혀보고 결과를 즉시 확인·저장·공유할 수 있다.",
        flows: [
          {
            step: "1",
            name: "사진 입력",
            desc: "갤러리에서 본인 전신/반신 사진 선택 또는 카메라 촬영",
            notes: ["얼굴 인식 불필요 — 신체 실루엣 기반 처리", "권장 비율: 3:4 세로"],
          },
          {
            step: "2",
            name: "옷 선택",
            desc: "시착할 의류 선택 — 3가지 경로 지원",
            notes: [
              "① 커뮤니티 피드 게시물의 태그 상품 직접 선택",
              "② 내 옷장에 저장된 아이템 선택",
              "③ 외부 상품 URL 붙여넣기 (상품 이미지 크롤링)",
            ],
          },
          {
            step: "3",
            name: "AI 생성",
            desc: "서버 사이드 AI 모델이 합성 이미지 생성",
            notes: ["처리 시간 목표: 10초 이내", "로딩 중 진행 애니메이션 표시", "실패 시 재시도 1회 자동"],
          },
          {
            step: "4",
            name: "결과 확인",
            desc: "원본 사진 ↔ 시착 결과 스와이프 비교",
            notes: ["결과 이미지 디바이스 저장", "옷장에 아이템 추가 (원클릭)", "커뮤니티 피드에 바로 공유"],
          },
        ],
        constraints: [
          "사용자 원본 사진은 시착 완료 후 서버에서 즉시 삭제 (개인정보 보호)",
          "생성 결과물은 30일 보관 후 자동 삭제 (미저장 시)",
          "1일 무료 시착 횟수: 5회 / 초과 시 추가 크레딧 안내",
        ],
        mvpOut: ["실시간 영상 시착 (카메라 스트리밍)", "다중 아이템 동시 시착 (상하의 조합)"],
      },
    },
  },
  {
    id: "closet",
    label: "개인 옷장",
    icon: "②",
    badge: "P2",
    content: {
      type: "feature",
      data: {
        title: "개인 옷장 (디지털 클로젯)",
        priority: "P2 — 핵심 리텐션 기능",
        goal: "사용자가 관심 있는 옷을 디지털 옷장에 모아두고 카테고리별로 관리할 수 있다.",
        flows: [
          {
            step: "1",
            name: "아이템 추가",
            desc: "옷장에 패션 아이템을 저장하는 3가지 경로",
            notes: [
              "① AI 시착 결과에서 '옷장에 추가' 버튼",
              "② 커뮤니티 피드 게시물의 태그 상품에서 저장",
              "③ 외부 URL / 이미지 직접 추가",
            ],
          },
          {
            step: "2",
            name: "아이템 정보",
            desc: "저장 시 자동 또는 수동으로 메타데이터 입력",
            notes: [
              "카테고리 (상의 / 하의 / 아우터 / 신발 / 액세서리)",
              "색상 태그 (자동 추출 + 수동 수정)",
              "브랜드명 / 외부 쇼핑몰 링크 (선택)",
              "개인 메모 (선택)",
            ],
          },
          {
            step: "3",
            name: "옷장 탐색",
            desc: "저장된 아이템을 다양한 방식으로 탐색",
            notes: [
              "카테고리 탭 필터",
              "색상 팔레트 필터",
              "그리드 뷰 (기본) / 리스트 뷰",
              "최근 추가순 / 자주 시착순 정렬",
            ],
          },
          {
            step: "4",
            name: "아이템 활용",
            desc: "옷장 아이템 기반 액션",
            notes: [
              "해당 아이템으로 즉시 AI 시착 실행",
              "외부 쇼핑몰 링크로 이동",
              "아이템 삭제 / 편집",
            ],
          },
        ],
        constraints: [
          "MVP 아이템 저장 상한: 계정당 200개",
          "아이템 이미지 크기: 최대 10MB",
          "옷장은 기본 비공개 — 공개 전환 옵션 제공",
        ],
        mvpOut: ["코디 조합 저장 (룩북)", "AI 기반 코디 추천", "옷장 공유 / 팔로우"],
      },
    },
  },
  {
    id: "feed",
    label: "커뮤니티 피드",
    icon: "③",
    badge: "P3",
    content: {
      type: "feature",
      data: {
        title: "커뮤니티 피드 + 시착 결과 공유",
        priority: "P3 — 성장 엔진",
        goal: "사용자들이 AI 시착 결과와 패션 사진을 피드에 공유하고 서로 반응할 수 있다.",
        flows: [
          {
            step: "1",
            name: "게시물 작성",
            desc: "피드에 올릴 콘텐츠 구성",
            notes: [
              "이미지 1–5장 (필수) — AI 시착 결과 또는 일반 패션 사진",
              "캡션 텍스트 (선택, 최대 300자)",
              "제품 태그 1개 이상 필수 (태그 없으면 업로드 불가)",
              "공개 / 비공개 설정",
            ],
          },
          {
            step: "2",
            name: "피드 탐색",
            desc: "홈 피드 구성 방식",
            notes: [
              "기본: 최신순 전체 피드 (MVP는 알고리즘 없이 시간순)",
              "탭 전환: 전체 / 팔로잉",
              "이미지 중심 그리드 레이아웃 (Instagram 스타일)",
              "무한 스크롤",
            ],
          },
          {
            step: "3",
            name: "게시물 상세",
            desc: "게시물 클릭 시 상세 뷰",
            notes: [
              "이미지 풀뷰 스와이프",
              "태그된 제품 목록 (탭하면 외부 링크)",
              "좋아요 / 댓글",
              "이 옷으로 AI 시착 해보기 버튼",
              "게시자 프로필 / 팔로우 버튼",
            ],
          },
          {
            step: "4",
            name: "소셜 인터랙션",
            desc: "피드 내 사용자 간 상호작용",
            notes: [
              "좋아요 (하트)",
              "댓글 (텍스트, 최대 200자)",
              "팔로우 / 언팔로우",
              "게시물 신고",
            ],
          },
        ],
        constraints: [
          "제품 태그 없는 게시물 업로드 차단 (UX 가이드 문구 안내)",
          "이미지당 최대 20MB, JPG/PNG/HEIC 지원",
          "댓글은 최대 2depth (대댓글까지)",
        ],
        mvpOut: ["탐색 탭 / 검색", "해시태그", "스토리 / 릴스형 콘텐츠", "알림 고도화"],
      },
    },
  },
  {
    id: "tag",
    label: "제품 태그",
    icon: "④",
    badge: "P4",
    content: {
      type: "feature",
      data: {
        title: "제품 태그 & 쇼핑 연결",
        priority: "P4 — 수익화 기반",
        goal: "게시물 이미지 위에 제품을 태그하고 외부 쇼핑몰로 바로 연결한다.",
        flows: [
          {
            step: "1",
            name: "태그 추가",
            desc: "게시물 작성 시 이미지에 제품 태그 핀 지정",
            notes: [
              "이미지 위 원하는 위치 탭 → 태그 핀 생성",
              "브랜드명 / 제품명 텍스트 입력",
              "외부 쇼핑몰 URL 입력 (필수)",
              "이미지당 최대 5개 태그",
            ],
          },
          {
            step: "2",
            name: "태그 표시",
            desc: "피드 및 상세 뷰에서 태그 노출 방식",
            notes: [
              "피드 그리드: 태그 존재 시 쇼핑백 아이콘 오버레이",
              "상세 뷰: 이미지 탭 시 핀 표시 / 다시 탭 시 숨김 토글",
              "하단 제품 리스트: 브랜드명 + 제품명 + 링크 버튼",
            ],
          },
          {
            step: "3",
            name: "쇼핑 연결",
            desc: "외부 쇼핑몰 이동 플로우",
            notes: [
              "태그 또는 리스트의 '구매하기' 버튼 탭",
              "인앱 브라우저로 외부 쇼핑몰 URL 열기",
              "뒤로가기 시 게시물 상세로 복귀",
            ],
          },
        ],
        constraints: [
          "URL 유효성 검사 필수 (http/https 형식)",
          "태그는 게시물 필수 요소 — 없으면 업로드 불가",
          "MVP에서 URL 안전성 검사는 기본 블랙리스트 필터만 적용",
        ],
        mvpOut: ["자동 상품 인식 / 크롤링", "제휴 링크 수익화 (어필리에이트)", "가격 정보 자동 표시"],
      },
    },
  },
  {
    id: "auth",
    label: "계정 & 프로필",
    icon: "◉",
    content: {
      type: "support",
      data: {
        title: "계정 & 프로필 (지원 기능)",
        goal: "서비스 이용을 위한 최소한의 계정 기능과 개인 프로필 페이지를 제공한다.",
        items: [
          {
            name: "회원가입 / 로그인",
            details: ["소셜 로그인: 카카오, 애플 (MVP 필수)", "이메일 로그인 (선택)", "비회원 피드 열람 가능 (시착·저장·공유는 로그인 필요)"],
          },
          {
            name: "프로필 페이지",
            details: [
              "프로필 이미지 + 닉네임 + 한줄 소개",
              "내 게시물 그리드",
              "팔로워 / 팔로잉 수",
              "내 옷장 바로가기 버튼",
            ],
          },
          {
            name: "설정",
            details: ["알림 ON/OFF (좋아요, 댓글, 팔로우)", "계정 공개/비공개", "로그아웃 / 회원탈퇴"],
          },
        ],
      },
    },
  },
  {
    id: "nonfunc",
    label: "비기능 요구사항",
    icon: "◇",
    content: {
      type: "nonfunc",
      data: [
        { category: "성능", items: ["피드 로딩: 첫 콘텐츠 2초 이내", "AI 시착 생성: 10초 이내 (95th percentile)", "이미지 Lazy Load 적용"] },
        { category: "보안 / 개인정보", items: ["사용자 원본 사진 시착 후 즉시 삭제", "HTTPS 필수", "개인정보처리방침 동의 온보딩 포함"] },
        { category: "접근성", items: ["이미지 alt 텍스트 필수", "최소 터치 타겟 44×44pt"] },
        { category: "MVP 제외 범위", items: ["검색 / 해시태그 탐색", "푸시 알림 고도화", "다중 아이템 동시 시착", "어필리에이트 수익화", "웹 버전"] },
      ],
    },
  },
];

const badgeColor = { P1: "#E8FF47", P2: "#B8F0C8", P3: "#C8E8FF", P4: "#F0C8E8" };

export default function App() {
  const [active, setActive] = useState("overview");
  const current = sections.find((s) => s.id === active);

  return (
    <div style={{ fontFamily: "'DM Sans', 'Pretendard', sans-serif", background: "#0E0E0E", minHeight: "100vh", color: "#F0EDE6", display: "flex", flexDirection: "column" }}>
      {/* Header */}
      <div style={{ borderBottom: "1px solid #222", padding: "20px 28px", display: "flex", alignItems: "center", justifyContent: "space-between", background: "#0E0E0E", position: "sticky", top: 0, zIndex: 10 }}>
        <div style={{ display: "flex", alignItems: "baseline", gap: 12 }}>
          <span style={{ fontSize: 20, fontWeight: 800, letterSpacing: "-0.5px", color: "#E8FF47" }}>FitCheck</span>
          <span style={{ fontSize: 12, color: "#555", letterSpacing: "0.1em", textTransform: "uppercase" }}>MVP 요구사항 기능서</span>
        </div>
        <div style={{ display: "flex", gap: 8 }}>
          {["P1","P2","P3","P4"].map(p => (
            <span key={p} style={{ background: badgeColor[p], color: "#111", fontSize: 10, fontWeight: 700, padding: "3px 8px", borderRadius: 4 }}>{p}</span>
          ))}
        </div>
      </div>

      <div style={{ display: "flex", flex: 1 }}>
        {/* Sidebar */}
        <nav style={{ width: 200, minWidth: 200, borderRight: "1px solid #1A1A1A", padding: "16px 0", display: "flex", flexDirection: "column", gap: 2, position: "sticky", top: 61, alignSelf: "flex-start", height: "calc(100vh - 61px)", overflowY: "auto" }}>
          {sections.map((s) => (
            <button key={s.id} onClick={() => setActive(s.id)} style={{ display: "flex", alignItems: "center", gap: 10, padding: "10px 20px", background: active === s.id ? "#1A1A1A" : "transparent", border: "none", borderLeft: active === s.id ? "2px solid #E8FF47" : "2px solid transparent", color: active === s.id ? "#F0EDE6" : "#555", cursor: "pointer", textAlign: "left", fontSize: 13, fontWeight: active === s.id ? 600 : 400, transition: "all 0.15s", width: "100%" }}>
              <span style={{ fontSize: 14, color: active === s.id ? "#E8FF47" : "#333" }}>{s.icon}</span>
              <span style={{ flex: 1 }}>{s.label}</span>
              {s.badge && <span style={{ background: badgeColor[s.badge], color: "#111", fontSize: 9, fontWeight: 700, padding: "2px 5px", borderRadius: 3 }}>{s.badge}</span>}
            </button>
          ))}
        </nav>

        {/* Main Content */}
        <main style={{ flex: 1, padding: "32px 40px", maxWidth: 860, overflowY: "auto" }}>
          {current?.content.type === "overview" && <OverviewSection data={current.content.data} />}
          {current?.content.type === "feature" && <FeatureSection data={current.content.data} />}
          {current?.content.type === "support" && <SupportSection data={current.content.data} />}
          {current?.content.type === "nonfunc" && <NonfuncSection data={current.content.data} />}
        </main>
      </div>
    </div>
  );
}

function OverviewSection({ data }) {
  return (
    <div>
      <div style={{ marginBottom: 32 }}>
        <div style={{ fontSize: 11, color: "#555", letterSpacing: "0.12em", textTransform: "uppercase", marginBottom: 8 }}>서비스 개요</div>
        <h1 style={{ fontSize: 36, fontWeight: 800, margin: 0, letterSpacing: "-1px" }}>{data.name}</h1>
        <p style={{ fontSize: 16, color: "#E8FF47", margin: "8px 0 0", fontWeight: 500 }}>{data.tagline}</p>
      </div>

      <div style={{ background: "#141414", border: "1px solid #222", borderRadius: 12, padding: 24, marginBottom: 24 }}>
        <p style={{ fontSize: 14, lineHeight: 1.75, color: "#AAA", margin: 0 }}>{data.description}</p>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr 1fr", gap: 16, marginBottom: 32 }}>
        {[
          { label: "타겟 사용자", value: data.target },
          { label: "플랫폼", value: data.platform },
          { label: "개발 범위", value: data.phase },
        ].map((item) => (
          <div key={item.label} style={{ background: "#141414", border: "1px solid #1E1E1E", borderRadius: 10, padding: "16px 20px" }}>
            <div style={{ fontSize: 10, color: "#444", textTransform: "uppercase", letterSpacing: "0.1em", marginBottom: 6 }}>{item.label}</div>
            <div style={{ fontSize: 13, fontWeight: 600, color: "#EEE" }}>{item.value}</div>
          </div>
        ))}
      </div>

      <div>
        <div style={{ fontSize: 11, color: "#555", letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: 16 }}>기능 우선순위</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
          {data.priorities.map((p) => (
            <div key={p.rank} style={{ display: "flex", alignItems: "center", gap: 16, background: "#141414", border: "1px solid #1E1E1E", borderRadius: 10, padding: "14px 20px" }}>
              <span style={{ width: 28, height: 28, borderRadius: "50%", background: p.color, color: "#111", fontSize: 12, fontWeight: 800, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>{p.rank}</span>
              <span style={{ fontSize: 14, fontWeight: 600 }}>{p.name}</span>
              <span style={{ marginLeft: "auto", fontSize: 10, color: "#444" }}>P{p.rank}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function FeatureSection({ data }) {
  const [openStep, setOpenStep] = useState(null);
  return (
    <div>
      <div style={{ marginBottom: 28 }}>
        <div style={{ display: "flex", alignItems: "center", gap: 12, marginBottom: 8 }}>
          <h2 style={{ fontSize: 26, fontWeight: 800, margin: 0, letterSpacing: "-0.5px" }}>{data.title}</h2>
          <span style={{ background: Object.values(badgeColor)[["P1","P2","P3","P4"].indexOf(data.priority.split(" ")[0])], color: "#111", fontSize: 11, fontWeight: 700, padding: "4px 10px", borderRadius: 6 }}>{data.priority.split(" ")[0]}</span>
        </div>
        <p style={{ fontSize: 12, color: "#888", margin: 0 }}>{data.priority.split("— ")[1]}</p>
      </div>

      <div style={{ background: "#141414", border: "1px solid #222", borderLeft: "3px solid #E8FF47", borderRadius: 10, padding: "14px 20px", marginBottom: 28 }}>
        <div style={{ fontSize: 10, color: "#555", textTransform: "uppercase", letterSpacing: "0.1em", marginBottom: 4 }}>목표</div>
        <p style={{ fontSize: 14, color: "#CCC", margin: 0, lineHeight: 1.7 }}>{data.goal}</p>
      </div>

      <div style={{ marginBottom: 32 }}>
        <div style={{ fontSize: 11, color: "#555", letterSpacing: "0.1em", textTransform: "uppercase", marginBottom: 14 }}>사용자 플로우</div>
        <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
          {data.flows.map((flow, i) => (
            <div key={i} style={{ background: "#141414", border: "1px solid #1E1E1E", borderRadius: 10, overflow: "hidden" }}>
              <button onClick={() => setOpenStep(openStep === i ? null : i)} style={{ width: "100%", display: "flex", alignItems: "center", gap: 14, padding: "14px 20px", background: "transparent", border: "none", color: "#F0EDE6", cursor: "pointer", textAlign: "left" }}>
                <span style={{ width: 24, height: 24, borderRadius: "50%", background: "#1E1E1E", border: "1px solid #333", fontSize: 11, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center", color: "#E8FF47", flexShrink: 0 }}>{flow.step}</span>
                <span style={{ flex: 1, fontSize: 14, fontWeight: 600 }}>{flow.name}</span>
                <span style={{ fontSize: 12, color: "#444" }}>{openStep === i ? "▲" : "▼"}</span>
              </button>
              {openStep === i && (
                <div style={{ padding: "0 20px 16px 58px", borderTop: "1px solid #1A1A1A" }}>
                  <p style={{ fontSize: 13, color: "#888", margin: "12px 0 10px" }}>{flow.desc}</p>
                  <ul style={{ margin: 0, padding: 0, listStyle: "none", display: "flex", flexDirection: "column", gap: 6 }}>
                    {flow.notes.map((n, j) => (
                      <li key={j} style={{ fontSize: 13, color: "#BBB", display: "flex", gap: 8, alignItems: "flex-start" }}>
                        <span style={{ color: "#E8FF47", flexShrink: 0, marginTop: 2 }}>›</span>
                        {n}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 16 }}>
        <div style={{ background: "#141414", border: "1px solid #1E1E1E", borderRadius: 10, padding: "16px 20px" }}>
          <div style={{ fontSize: 10, color: "#666", textTransform: "uppercase", letterSpacing: "0.1em", marginBottom: 12 }}>⚠ 제약 조건</div>
          <ul style={{ margin: 0, padding: 0, listStyle: "none", display: "flex", flexDirection: "column", gap: 8 }}>
            {data.constraints.map((c, i) => (
              <li key={i} style={{ fontSize: 12, color: "#999", lineHeight: 1.5, display: "flex", gap: 8, alignItems: "flex-start" }}>
                <span style={{ color: "#FF6B6B", flexShrink: 0, marginTop: 2 }}>!</span>
                {c}
              </li>
            ))}
          </ul>
        </div>
        <div style={{ background: "#141414", border: "1px solid #1E1E1E", borderRadius: 10, padding: "16px 20px" }}>
          <div style={{ fontSize: 10, color: "#666", textTransform: "uppercase", letterSpacing: "0.1em", marginBottom: 12 }}>✕ MVP 제외</div>
          <ul style={{ margin: 0, padding: 0, listStyle: "none", display: "flex", flexDirection: "column", gap: 8 }}>
            {data.mvpOut.map((o, i) => (
              <li key={i} style={{ fontSize: 12, color: "#555", lineHeight: 1.5, display: "flex", gap: 8, alignItems: "flex-start", textDecoration: "line-through" }}>
                {o}
              </li>
            ))}
          </ul>
        </div>
      </div>
    </div>
  );
}

function SupportSection({ data }) {
  return (
    <div>
      <div style={{ marginBottom: 28 }}>
        <h2 style={{ fontSize: 26, fontWeight: 800, margin: 0, letterSpacing: "-0.5px" }}>{data.title}</h2>
      </div>
      <div style={{ background: "#141414", border: "1px solid #222", borderLeft: "3px solid #555", borderRadius: 10, padding: "14px 20px", marginBottom: 28 }}>
        <p style={{ fontSize: 14, color: "#CCC", margin: 0, lineHeight: 1.7 }}>{data.goal}</p>
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        {data.items.map((item, i) => (
          <div key={i} style={{ background: "#141414", border: "1px solid #1E1E1E", borderRadius: 10, padding: "16px 20px" }}>
            <div style={{ fontSize: 13, fontWeight: 700, color: "#EEE", marginBottom: 10 }}>{item.name}</div>
            <ul style={{ margin: 0, padding: 0, listStyle: "none", display: "flex", flexDirection: "column", gap: 6 }}>
              {item.details.map((d, j) => (
                <li key={j} style={{ fontSize: 13, color: "#888", display: "flex", gap: 8 }}>
                  <span style={{ color: "#444" }}>›</span>{d}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
}

function NonfuncSection({ data }) {
  return (
    <div>
      <div style={{ marginBottom: 28 }}>
        <h2 style={{ fontSize: 26, fontWeight: 800, margin: 0, letterSpacing: "-0.5px" }}>비기능 요구사항</h2>
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
        {data.map((section, i) => (
          <div key={i} style={{ background: "#141414", border: "1px solid #1E1E1E", borderRadius: 10, padding: "16px 20px" }}>
            <div style={{ fontSize: 11, color: "#555", textTransform: "uppercase", letterSpacing: "0.1em", marginBottom: 10 }}>{section.category}</div>
            <ul style={{ margin: 0, padding: 0, listStyle: "none", display: "flex", flexDirection: "column", gap: 7 }}>
              {section.items.map((item, j) => (
                <li key={j} style={{ fontSize: 13, color: section.category === "MVP 제외 범위" ? "#444" : "#999", display: "flex", gap: 8, textDecoration: section.category === "MVP 제외 범위" ? "line-through" : "none" }}>
                  <span style={{ color: section.category === "MVP 제외 범위" ? "#333" : "#E8FF47", flexShrink: 0 }}>›</span>
                  {item}
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </div>
  );
}

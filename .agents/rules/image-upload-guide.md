# 이미지 업로드 서비스 구현 가이드

이 문서는 헥사고날 아키텍처 기반의 `simul` 프로젝트에서 이미지 업로드 기능을 구현하고 사용할 때 참고해야 하는 가이드라인입니다.

## 1. 아키텍처 원칙 (왜 `common` 도메인에 있는가?)

*   **인프라 추상화**: 파일을 로컬 디스크나 S3 등 물리적/클라우드 저장소에 저장하는 '기술적 행위'는 도메인에 관계없이 동일합니다. 코드 중복을 방지하고 향후 S3로 쉽게 전환하기 위해 인프라 로직은 `common` 도메인에서 관리합니다.
*   **관심사 분리**:
    *   **`common` 도메인**: 파일의 물리적 저장, 파일명 난수화(UUID), 디렉토리 분류 등 인프라 레벨의 역할을 담당합니다.
    *   **각 비즈니스 도메인 (`post`, `tryon` 등)**: 파일 크기, 해상도, 업로드 개수 등 **비즈니스 규칙 검증**을 담당합니다.

## 2. 공통 기능 명세

*   **포트 (Interface)**: `com.simul.common.application.port.out.ImageStoragePort`
*   **어댑터 (Implementation)**: `com.simul.common.adapter.out.storage.LocalImageStorageAdapter`
    *   현재는 로컬 파일 시스템을 사용하도록 구현되어 있습니다.
    *   `application.yml`의 `simul.storage.local` 설정을 기반으로 동작합니다.

## 3. 다른 도메인에서 이미지 업로드 기능을 사용하는 방법

새로운 도메인(예: 게시물, 옷장 등)에서 이미지를 업로드해야 하는 경우, 아래 절차에 따라 개발을 진행하세요.

### Step 1. Port 주입
해당 도메인의 Service(UseCase 구현체)에서 `ImageStoragePort`를 의존성 주입(DI) 받습니다.

```java
@Service
@RequiredArgsConstructor
public class TryOnService implements CreateTryOnUseCase {
    private final ImageStoragePort imageStoragePort;
    // ...
}
```

### Step 2. 비즈니스 로직 검증
`ImageStoragePort`를 호출하기 전에, 해당 도메인의 정책에 맞게 파일을 검증합니다.
*(예: 해상도 제한, 용량 제한, 이미지 장수 제한 등)*

### Step 3. 업로드 수행 (`uploadImage` 호출)
검증이 끝난 파일을 `ImageStoragePort.uploadImage()` 메서드를 사용하여 저장합니다.
이때 두 번째 인자인 **`directoryPrefix`** 에 해당 도메인 이름(예: `"tryon"`, `"post"`, `"closet"`)을 전달해야 합니다.

```java
// 파일과 도메인 접두사를 전달하여 업로드 수행
String imageUrl = imageStoragePort.uploadImage(file, "tryon");
```

### Step 4. DB 저장
반환된 `imageUrl`은 클라이언트가 접근 가능한 상대 경로(예: `/uploads/images/tryon/2026/05/04/uuid.jpg`)입니다. 이 경로를 해당 도메인의 엔티티에 저장합니다.

## 4. 주의사항

*   **파일명 충돌 방지**: `LocalImageStorageAdapter` 내부에서 원본 파일명을 고유한 UUID로 자동 변경하여 저장하므로, 별도로 파일명을 난수화할 필요가 없습니다.
*   **디렉토리 분류**: `directoryPrefix` 파라미터를 통해 도메인별 폴더가 자동 생성 및 분류되므로, 정확한 도메인 명칭을 사용해 주세요.
*   **S3 전환 고려**: 향후 S3 저장소로 전환되더라도 `ImageStoragePort` 인터페이스는 변경되지 않으므로, 각 도메인의 Service 코드는 전혀 수정할 필요가 없습니다.

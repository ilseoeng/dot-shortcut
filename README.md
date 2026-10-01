# dot 바로가기 · dot Shortcut

**홈 화면에서 내 Your dot 채팅과 통화를 빠르게 여는 안드로이드 바로가기 앱.**

ChatGPT 앱을 열고 dot을 찾는 과정을 줄여줍니다. 새로운 AI를 만들거나 대화를 별도로 저장하지 않습니다. OpenAI 공식 앱이 아닌 독립적인 오픈소스 도구입니다.

[APK 다운로드](https://github.com/ilseoeng/dot-shortcut/releases) · [개인정보 안내](PRIVACY.md) · [보안 검토](SECURITY.md) · [문제 제보](https://github.com/ilseoeng/dot-shortcut/issues)

## 주요 기능

- **dot 채팅**: 홈 화면 아이콘으로 ChatGPT의 dot 화면 열기.
- **dot 통화**: dot 화면을 연 뒤 접근성 서비스로 통화 버튼 자동 선택.
- **자동 연결 테스트**: dot 화면과 통화 버튼을 확인합니다. 테스트 중에는 통화를 걸지 않습니다.
- **바로가기 추가**: 자동 연결 확인 성공 후 추가 버튼이 활성화됩니다.
- **어시스턴트 연결**: 지원되는 기기에서 기본 디지털 어시스턴트 호출을 채팅 또는 통화에 연결합니다.

## 이용 조건

이 앱은 Android 8.0(API 26) 이상을 대상으로 합니다. **해당 기기에서 지원되는 ChatGPT 안드로이드 앱이 설치되어 있어야 하며, 로그인한 계정에서 Your dot을 사용할 수 있어야 합니다.** dot 기능이 없는 계정에 기능을 활성화해 주지는 않습니다. 별도 OpenAI API 키나 이 앱의 회원가입은 필요하지 않습니다.

첫 공개 버전은 **v0.1.0 베타**입니다. Samsung SM-S931N, Android API 36, ChatGPT 1.2026.265에서 채팅과 접근성 통화를 사용자와 확인했습니다. 모든 기기·계정·언어에서 동작을 보장하지 않습니다. 자동 연결 테스트는 상태 판정 단위 테스트를 거쳤으며, 다양한 실기기 검증은 진행 중입니다.

## 설치와 첫 설정

1. [Releases](https://github.com/ilseoeng/dot-shortcut/releases)에서 `dot-shortcut-0.1.0.apk`를 다운로드합니다.
2. APK를 열어 설치합니다. Android가 요청하면 다운로드에 사용한 브라우저 또는 파일 앱에 설치를 허용합니다.
3. **dot 바로가기** 앱을 열고 **연결 테스트**를 누릅니다.
4. 접근성이 꺼져 있으면 **접근성 설정 열기 → 설치된 앱 → dot 바로가기 → 사용 켜기** 순서로 설정합니다. 시스템 권한 설명을 확인하세요. 기기에 따라 명칭이 다릅니다.
5. 앱으로 돌아와 **연결 테스트**를 다시 누릅니다. ChatGPT의 dot 화면이 열리면 최대 15초 동안 화면을 확인합니다. 확인 후 이 앱으로 돌아옵니다.
6. **연결 확인됨**이 표시되면 채팅 또는 통화를 선택하고 **홈 화면에 바로가기 추가**를 누릅니다.

앱을 한 번 연 뒤 런처에서 앱 아이콘을 길게 누르면 채팅·통화 바로가기 메뉴도 사용할 수 있습니다. 런처에 따라 메뉴나 고정 기능이 다를 수 있습니다.

### 접근성이 ‘제한된 설정’으로 차단될 때

APK로 설치한 앱은 Android에서 접근성이 제한될 수 있습니다. 앱과 소스를 신뢰하는 경우에만 **설정 → 앱 → dot 바로가기 → 더보기 → 제한된 설정 허용**을 확인한 뒤 접근성 설정을 다시 진행하세요. 제조사·버전에 따라 경로가 다르거나 제공되지 않을 수 있습니다. 시스템 보호 기능을 끄는 방식은 안내하지 않습니다.

참고: [Google의 제한된 설정 안내](https://support.google.com/android/answer/12623953?hl=ko).

### 어시스턴트 설정

앱에서 **어시스턴트로 설정**을 누르면 지원되는 설정 화면이 열립니다. 디지털 어시스턴트 앱으로 dot 바로가기를 선택하고 호출 시 동작을 정합니다.

**일부 삼성 기기에서는 선택 목록에 앱이 표시되지 않을 수 있습니다.** 개발 중 삼성에서는 adb로 역할을 지정한 뒤 호출을 확인했습니다. 현재 공개 APK의 수정된 시스템 권한 선언이 모든 기기의 선택 문제를 해결한다고 보장하지 않습니다. 목록에 없으면 홈 화면 바로가기를 이용하세요. 어시스턴트 설정은 필수가 아닙니다.

## 동작과 개인정보

- dot 진입 링크는 `https://chatgpt.com/app/o`입니다. 특정 사용자 ID·이메일·계정 토큰을 포함하지 않습니다.
- 접근성은 ChatGPT 화면에서 dot 식별 표시와 통화 버튼을 찾는 데 사용합니다. 사용자가 연결 테스트 또는 통화 바로가기를 실행한 뒤 최대 15초 동안 확인합니다.
- 이 앱은 대화 내용·음성·로그인 토큰을 저장하거나 전송하지 않습니다. 인터넷·마이크·연락처·위치·전체 앱 목록 권한을 요청하지 않습니다.
- 설정과 연결 확인 시각은 기기 내부에 저장합니다. ChatGPT가 처리하는 대화와 음성에는 ChatGPT의 개인정보 정책이 적용됩니다.
- 통화 바로가기는 앱에서 발급한 설치별 확인 토큰이 필요하며, 잠긴 기기에서는 자동 실행하지 않습니다.
- [개인정보 안내 전문](PRIVACY.md).

## 알려진 제한

- `/app/o`는 공식 안정 API로 문서화된 연결 경로가 아닙니다. ChatGPT 업데이트로 동작이 바뀔 수 있습니다.
- 통화 버튼 자동 선택은 ChatGPT 화면 구조와 라벨에 의존합니다. 현재 `통화`, `Start call`, `Start Call`을 인식합니다.
- 로그인·dot 활성화 여부를 공식 API로 조회하지 않습니다. 시간 초과는 **연결을 확인하지 못함**이며, 로그인 실패나 계정 자격 부족을 확정하는 결과가 아닙니다.
- 성공 기록은 마지막 테스트 결과입니다. ChatGPT에서 계정을 바꾸거나 로그아웃한 뒤에는 다시 테스트하세요.
- 접근성을 꺼두면 자동 연결 테스트와 통화 자동 선택을 사용할 수 없고, 앱의 추가 버튼도 비활성화됩니다.
- 자동 확인 실패 시 통화를 임의로 실행하지 않습니다. 일반 ChatGPT 음성 대화로 대체하지 않습니다.
- 이전 개발용 디버그 APK와 배포용 APK는 서명이 다릅니다. 개발용 앱이 설치되어 있다면 먼저 삭제한 뒤 공개 APK를 설치하세요. 설정과 바로가기를 다시 만들어야 할 수 있습니다.

## 개발

Kotlin · Jetpack Compose · Android Intent · ShortcutManager · AccessibilityService · DataStore.

JDK 17, Android SDK 36이 필요합니다. `local.properties`에 자신의 SDK 경로를 지정하세요.

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
# Windows: gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

배포 빌드는 `DOT_SIGNING_PROPERTIES` 환경 변수로 **저장소 밖의** 서명 설정 파일을 지정합니다. 파일에는 `storeFile`, `storePassword`, `keyAlias`, `keyPassword`가 필요합니다. 키와 암호를 커밋하지 마세요. 환경 변수가 없으면 release APK는 서명되지 않습니다.

```sh
./gradlew testDebugUnitTest lintRelease assembleRelease
```

같은 서명 키를 사용해야 기존 설치에 업데이트할 수 있습니다. 공개 릴리스에는 APK, SHA-256 체크섬 및 인증서 지문을 제공합니다.

## English

dot Shortcut is an independent Android launcher for an existing Your dot in ChatGPT. It opens dot chat and uses a narrowly scoped accessibility service to select its call button. A connection test observes the dot screen without starting a call; successful verification enables the pin-shortcut button.

Requires a supported ChatGPT Android installation and an account with an existing Your dot. No API key or separate login. Device compatibility, assistant selection, and ChatGPT UI changes can affect behavior. The app does not transmit data or request internet/microphone permissions. Read [PRIVACY.md](PRIVACY.md) and the limitations above before enabling accessibility.

## 라이선스

프로젝트 소스는 MIT이며 외부 라이브러리는 [각 라이선스](THIRD_PARTY.md)를 따릅니다. Your dot, ChatGPT 및 OpenAI의 상표와 서비스는 각 권리자에게 속합니다. 이 프로젝트는 OpenAI와 제휴하거나 공식 지원을 받지 않습니다.

Git 프로필 및 remote 권한을 확인한 뒤 commit & push를 수행합니다.

## 입력
- $ARGUMENTS: 커밋 메시지 (없으면 변경 내용을 분석하여 자동 생성)

## 절차

### 1. Git 프로필 확인 (필수)
아래 명령으로 현재 로컬 저장소의 git user를 확인한다.
```
git config user.name
git config user.email
```

**허용 프로필:**
- user.name: `alanhakhyeonsong`
- user.email: `songs4805@naver.com`

프로필이 일치하지 않으면:
- 현재 설정된 프로필 정보를 사용자에게 보여준다.
- **절대 commit/push를 진행하지 않는다.**
- 아래 수정 명령어를 안내한다.
```
git config user.name "alanhakhyeonsong"
git config user.email "songs4805@naver.com"
```
- 수정 후 다시 `/safe-push`를 실행하라고 안내한다.

### 2. Remote 저장소 소유권 확인 (필수)
아래 명령으로 remote URL을 확인한다.
```
git remote -v
```

**허용 remote:**
- `origin`이 `alanhakhyeonsong/LetsReadBooks` 저장소를 가리켜야 한다.
- HTTPS: `https://github.com/alanhakhyeonsong/LetsReadBooks.git`
- SSH: `git@github.com:alanhakhyeonsong/LetsReadBooks.git`

remote가 다른 저장소를 가리키고 있으면:
- 현재 remote 정보를 사용자에게 보여준다.
- **절대 commit/push를 진행하지 않는다.**
- "이 저장소의 remote가 예상과 다릅니다. 확인 후 다시 시도하세요."라고 안내한다.

### 3. 최근 커밋 작성자 검증 (경고)
아래 명령으로 최근 5개 커밋의 작성자를 확인한다.
```
git log --format="%an <%ae>" -5
```

`alanhakhyeonsong` 외의 작성자가 포함되어 있으면:
- **경고 메시지**를 출력한다: "최근 커밋에 다른 작성자의 커밋이 포함되어 있습니다."
- 해당 커밋 목록을 보여준다.
- 사용자에게 계속 진행할지 확인한다.

### 4. 변경 사항 확인
위 검증이 모두 통과하면 아래를 병렬 실행한다.
- `git status` (untracked 파일 확인)
- `git diff` (staged + unstaged 변경 확인)
- `git log --oneline -5` (최근 커밋 스타일 확인)

### 5. 커밋
- $ARGUMENTS가 있으면 해당 메시지로 커밋한다.
- 없으면 변경 내용을 분석하여 CLAUDE.md의 커밋 컨벤션에 맞는 메시지를 자동 생성한다.
- 커밋 메시지 끝에 반드시 아래를 포함한다:
```
Co-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>
```

### 6. 푸시
- `git push`를 실행한다.
- 성공 시 커밋 해시와 메시지를 출력한다.

## 주의사항
- 프로필 확인을 **절대 건너뛰지 않는다.**
- NHN 업무 계정(`songs4805@injeinc.co.kr`)으로 설정되어 있으면 반드시 중단한다.
- remote가 `alanhakhyeonsong/LetsReadBooks`가 아니면 반드시 중단한다.
- 위 검증 단계(1, 2)를 하나라도 통과하지 못하면 이후 단계를 **절대 실행하지 않는다.**

## GitHub 브랜치 보호 규칙 (서버 측)
본 저장소의 `master` 브랜치에는 아래 보호 규칙이 적용되어 있다.

- **PR 필수**: 외부 기여자(fork한 사람)는 `master`에 직접 push할 수 없으며, 반드시 PR을 생성해야 한다.
- **승인 1명 필수**: PR은 최소 1명의 리뷰 승인이 있어야 머지 가능하다.
- **Stale review 자동 해제**: PR에 새 커밋이 추가되면 기존 승인이 자동으로 해제된다.
- **Force push 금지**: `master` 브랜치에 대한 force push가 차단되어 있다.
- **소유자 예외**: `enforce_admins: false`이므로 저장소 소유자(`alanhakhyeonsong`)는 위 규칙을 우회하여 직접 push할 수 있다.

이 규칙은 GitHub API로 설정되었으며, 변경이 필요하면 GitHub Settings → Branches에서 수정한다.

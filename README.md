# AutoSavePlugin 🛠️

마인크래프트 서버에서 원하는 시간마다 월드 자동 저장과 백업을 간편하게 할 수 있는 Spigot/Paper 플러그인입니다.

---

## 주요 기능 ✨

- `/autosave backup`  
  지정한 월드를 `tar.gz` 형식으로 백업합니다.  
  월드 이름을 생략하면 서버의 모든 월드를 백업합니다. 🌍

- `/autosave reload`  
  플러그인의 설정 파일(`config.yml`, `msgconfig.yml`)을 다시 불러옵니다. 🔄

- 탭 자동완성 지원 (`backup`, `reload` 및 월드 이름) ⌨️

- 백업 중에도 서버 메인 스레드에서 월드 저장을 처리하여 안전한 백업 가능 💾🛡️

- 사용자 메시지 `msgconfig.yml`을 통해 메시지 커스터마이징 가능 📝

---

## 설정 파일 ⚙️

### `config.yml`

```yaml
autosave:
  interval: 300
```

---
### `msgconfig.yml`
```yaml
backup:
  start: "&9&l[AutoSave]&f &aStarting backup of world '%world%'."
  startall: "&9&l[AutoSave]&f &aStarting backup for all worlds on the server."
  done: "&9&l[AutoSave]&f &aBackup of world '%world%' completed. File saved to : %path%"
  error: "&9&l[AutoSave]&f &cAn error occurred during backup: %error%"
  notfound: "&9&l[AutoSave]&f &cWorld '%world%' not found."

save:
  done: "&9&l[AutoSave]&f &aWorld save completed."
  start: "&9&l[AutoSave]&f Starting auto-save every %interval% seconds."
  stop: "&9&l[AutoSave]&f Auto-save stopped."

reload:
  done: "&9&l[AutoSave]&f &aConfiguration successfully reloaded."

unknown:
  command: "&9&l[AutoSave]&f &cUnknown subcommand."

usage: "&9&l[AutoSave]&f &cUsage: /autosave backup or /autosave reload"

nopermission: "&9&l[AutoSave]&f &cYou do not have permission to use this command."
```
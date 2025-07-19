# AutoSavePlugin

**AutoSavePlugin**은 마인크래프트 서버에서 자동 저장 및 월드 백업 기능을 제공하는 Spigot/Paper 플러그인입니다.  
**AutoSavePlugin** is a Spigot/Paper plugin that provides automatic world saving and backup functionality for Minecraft servers.

---

## 🔧 주요 기능 | Features

- ✅ 서버 월드 자동 저장 (기본 5분 간격)  
  ✅ Automatic world saving (default every 5 minutes)

- ✅ `/backupworld <월드이름>` 명령어로 tar.gz 압축 백업  
  ✅ `/backupworld <world>` command for tar.gz world backups

- ✅ `msgconfig.yml`을 통한 안내 메시지 커스터마이징  
  ✅ Customizable messages via `msgconfig.yml`

- ✅ 비동기 백업 처리로 서버 렉 최소화  
  ✅ Asynchronous backup to reduce lag

---

## 📁 설치 방법 | Installation

1. `AutoSavePlugin.jar`를 `plugins/` 폴더에 넣습니다.  
   Put `AutoSavePlugin.jar` into your server’s `plugins/` folder.

2. 서버를 시작합니다.  
   Start your server.

3. `config.yml`과 `msgconfig.yml`이 자동 생성됩니다.  
   The plugin will generate `config.yml` and `msgconfig.yml` automatically.

---

## ⚙️ 설정 | Configuration

### `config.yml`

```yaml
autosave:
  interval: 300  # 자동 저장 주기 (초 단위) / Auto-save interval in seconds
```
---
### `msgconfig.yml`
```yaml
backup:
  start: "§9§l[AutoSave]§f §aStarting backup of world '%world%'."
  done: "§9§l[AutoSave]§f §aBackup of world '%world%' completed. File saved to : "
  error: "§9§l[AutoSave]§f §cAn error occurred during backup: %error%"
  notfound: "§9§l[AutoSave]§f §cWorld '%world%' not found."
  usage: "§9§l[AutoSave]§f §cUsage: /backupworld <worldname>"

saved:
  done: "§9§l[AutoSave]§f §aWorld save completed."
  start: "§9§l[AutoSave]§f Starting auto-save every %interval% seconds."
  stop: "§9§l[AutoSave]§f Auto-save stopped."
  usage: "§9§l[AutoSave]§f §cUsage: /autosavereload" # not used

reload:
  done: "§9§l[AutoSave]§f §aConfiguration successfully reloaded."
```
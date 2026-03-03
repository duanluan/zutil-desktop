# ZUI Local/Remote Switch

`zutil-desktop` can switch between:

- remote published dependency: `top.zhjh:zui-compose-desktop`
- local composite build: `../zui-compose-desktop`

## One-off local run (no file change)

```powershell
.\gradlew.bat :composeApp:run -PuseLocalZuiComposeDesktop=true
```

## Persistent switch (edit `gradle.properties`)

Use scripts from project root:

Windows PowerShell:

```powershell
.\scripts\use-local-zui.ps1
.\scripts\use-remote-zui.ps1
```

Windows CMD:

```bat
scripts\use-local-zui.bat
scripts\use-remote-zui.bat
```

macOS/Linux:

```bash
./scripts/use-local-zui.sh
./scripts/use-remote-zui.sh
```

## Config keys

In `gradle.properties`:

- `useLocalZuiComposeDesktop=false`
- `localZuiComposeDesktopDir=../zui-compose-desktop`

When local mode is enabled, `settings.gradle.kts` will include the local build and substitute:

- `top.zhjh:zui-compose-desktop` -> `project(":zui")` from `zui-compose-desktop`

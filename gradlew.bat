@echo off
setlocal EnableExtensions
cd /d "%~dp0"
set "ROOT=%~dp0"
set "TASKS=%*"
if "%~1"=="" set "TASKS=build"

if not exist "%ROOT%targets\forge-1.20.1\gradlew.bat" (
    echo ERROR: Missing Blast Travel build targets.
    exit /b 1
)

for %%T in (forge-1.20.1 neoforge-1.21.1) do (
    echo.
    echo ============================================================
    echo Building %%T
    echo ============================================================
    pushd "%ROOT%targets\%%T"
    call gradlew.bat %TASKS%
    if errorlevel 1 (
        popd
        echo ERROR: %%T failed. Try Build-All.bat for detailed logging.
        exit /b 1
    )
    popd
)

powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT%scripts\build.ps1" -CollectOnly
exit /b %ERRORLEVEL%

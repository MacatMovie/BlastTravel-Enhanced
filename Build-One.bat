@echo off
rem Keep the interactive console open when launched from Explorer (cmd /c).
if not defined BLASTTRAVEL_BUILD_ONE_KEEP_OPEN (
    echo(%CMDCMDLINE% | findstr /I /C:" /c " >nul
    if not errorlevel 1 (
        set "BLASTTRAVEL_BUILD_ONE_KEEP_OPEN=1"
        start "" "%ComSpec%" /k ""%~f0" %*"
        exit /b
    )
)

setlocal EnableExtensions DisableDelayedExpansion
cd /d "%~dp0"
if not exist "%~dp0logs" mkdir "%~dp0logs"

echo ============================================================
echo Blast Travel - BUILD SELECTED
echo ============================================================
echo.
echo   A) Forge    1.20.1
echo   B) NeoForge 1.21.1
echo   C) Fabric   1.20.1
echo   D) Fabric   1.21.1
echo.
echo Example: C,D
echo.
if "%~1"=="" (
    set /p "SELECTION=Build target(s): "
) else (
    set "SELECTION=%~1"
)
if not defined SELECTION (
    echo No target selected.
    pause
    exit /b 1
)

if /I "%~2"=="-Clean" goto CleanBuild
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build-selected.ps1" -Selection "%SELECTION%"
goto BuildFinished

:CleanBuild
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build-selected.ps1" -Selection "%SELECTION%" -Clean

:BuildFinished
set "EXITCODE=%ERRORLEVEL%"
echo.
if "%EXITCODE%"=="0" echo SELECTED BUILDS FINISHED SUCCESSFULLY.
if not "%EXITCODE%"=="0" echo SELECTED BUILDS FINISHED WITH ERRORS. See logs\build-one-latest.log
pause
exit /b %EXITCODE%

@echo off
setlocal
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0use-remote-zui.ps1"
exit /b %errorlevel%

@echo off
echo =========================================
echo Starting Spring Boot (Profile: local)...
echo =========================================

if exist "..\.env" (
    for /f "tokens=1,* delims==" %%a in ('type ..\.env ^| findstr /v "^#" ^| findstr /v "^$"') do (
        set "%%a=%%b"
    )
)

set SPRING_PROFILES_ACTIVE=local
call gradlew.bat bootRun

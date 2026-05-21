@echo off
REM =============================================================================
REM sonar-scan.bat — Build all modules, generate coverage, push to SonarQube
REM =============================================================================
REM Usage:
REM   sonar-scan.bat                          prompts for token
REM   set SONAR_TOKEN=sqa_78172af3ca1722a6590e10ad5574e10662eac44c && sonar-scan.bat   non-interactive
REM   sonar-scan.bat --skip-tests             skip unit tests
REM =============================================================================
setlocal enabledelayedexpansion

set "SONAR_HOST=%SONAR_HOST%"
if "!SONAR_HOST!"=="" set "SONAR_HOST=http://localhost:9000"

set "SKIP_TESTS=false"
set "MAVEN_SKIP="

REM ---- Parse arguments -------------------------------------------------------
for %%A in (%*) do (
  if "%%A"=="--skip-tests" (
    set "SKIP_TESTS=true"
    set "MAVEN_SKIP=-DskipTests"
  )
)

REM ---- Require a token -------------------------------------------------------
if "!SONAR_TOKEN!"=="" (
  echo.
  echo SonarQube token is required.
  echo   1. Open !SONAR_HOST! ^(admin / admin on first launch^)
  echo   2. My Account -^> Security -^> Generate Token
  echo   3. Re-run:  set SONAR_TOKEN=^<token^) ^&^& sonar-scan.bat
  echo.
  set /p SONAR_TOKEN="sqa_78172af3ca1722a6590e10ad5574e10662eac44c"
  echo.
)

REM ---- Wait for SonarQube ----------------------------------------------------
echo ^>^>^> Waiting for SonarQube at !SONAR_HOST! ...
set WAITED=0
:WAIT_LOOP
curl -sf "!SONAR_HOST!/api/system/status" 2>nul | findstr /C:"\"status\":\"UP\"" >nul
if %errorlevel%==0 goto SONAR_READY
if !WAITED! GEQ 120 (
  echo ERROR: SonarQube did not become ready within 120s. Is Docker running?
  exit /b 1
)
timeout /t 5 /nobreak >nul
set /a WAITED=!WAITED!+5
echo   ... still waiting ^(!WAITED!s^)
goto WAIT_LOOP

:SONAR_READY
echo ^>^>^> SonarQube is UP.

REM ---- Maven build + analysis ------------------------------------------------
if "!SKIP_TESTS!"=="true" (
  echo ^>^>^> Skipping unit tests ^(no coverage data will be sent^).
)

echo.
echo ^>^>^> Running: mvn clean verify sonar:sonar
echo     Host   : !SONAR_HOST!
echo.

call mvn clean verify sonar:sonar ^
  -Dsonar.host.url="!SONAR_HOST!" ^
  -Dsonar.token="!SONAR_TOKEN!" ^
  !MAVEN_SKIP! ^
  --batch-mode ^
  --no-transfer-progress

if %errorlevel% neq 0 (
  echo.
  echo ERROR: Maven build or Sonar analysis failed. See output above.
  exit /b %errorlevel%
)

echo.
echo ======================================================
echo  Analysis complete. Open the dashboard:
echo  !SONAR_HOST!/projects
echo ======================================================
endlocal

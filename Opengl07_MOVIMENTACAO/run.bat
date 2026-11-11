@echo off
setlocal
rem ============================================================
rem  Compila e executa o projeto Opengl07_MOVIMENTACAO (main.Main3D) no Windows.
rem  Fica na raiz do Opengl07_MOVIMENTACAO, junto com src, shaders,
rem  as texturas (.png/.jpeg), o explode.gif e o x-35_obj.obj.
rem
rem  Na primeira vez baixa os jars do LWJGL para a pasta lib\
rem  (mesmas versoes do pom.xml) e depois compila/executa com javac/java.
rem ============================================================

rem Roda sempre a partir da pasta deste .bat, para os caminhos relativos (shaders\, texturas, obj, gif) funcionarem
cd /d "%~dp0"
if not exist "src\main\java\main\Main3D.java" (
    echo [ERRO] Projeto nao encontrado em "%~dp0"
    goto fim
)

rem ---- procura o JDK: PATH, depois JAVA_HOME, depois os JDKs baixados pela IDE (%USERPROFILE%\.jdks) ----
rem Guarda a pasta bin do JDK encontrado para usar o java da MESMA instalacao do javac
rem (evita pegar outro java/JRE que esteja antes no PATH).
set "JDKBIN="
for /f "delims=" %%j in ('where javac 2^>nul') do if not defined JDKBIN set "JDKBIN=%%~dpj"
if defined JDKBIN goto temJdk
if exist "%JAVA_HOME%\bin\javac.exe" set "JDKBIN=%JAVA_HOME%\bin\"
if defined JDKBIN goto temJdk
for /d %%d in ("%USERPROFILE%\.jdks\*") do if exist "%%d\bin\javac.exe" set "JDKBIN=%%d\bin\"
if defined JDKBIN goto temJdk
echo [ERRO] javac nao encontrado. Instale um JDK 8+ e coloque a pasta bin no PATH.
goto fim

:temJdk
echo == Usando JDK em %JDKBIN% ==

set LWJGL=3.3.3
set NAT=natives-windows
if /i "%PROCESSOR_ARCHITECTURE%"=="ARM64" set NAT=natives-windows-arm64
if not exist lib mkdir lib

call :baixa org/lwjgl/lwjgl/%LWJGL% lwjgl-%LWJGL%.jar
if errorlevel 1 goto erro
call :baixa org/lwjgl/lwjgl/%LWJGL% lwjgl-%LWJGL%-%NAT%.jar
if errorlevel 1 goto erro
call :baixa org/lwjgl/lwjgl-glfw/%LWJGL% lwjgl-glfw-%LWJGL%.jar
if errorlevel 1 goto erro
call :baixa org/lwjgl/lwjgl-glfw/%LWJGL% lwjgl-glfw-%LWJGL%-%NAT%.jar
if errorlevel 1 goto erro
call :baixa org/lwjgl/lwjgl-opengl/%LWJGL% lwjgl-opengl-%LWJGL%.jar
if errorlevel 1 goto erro
call :baixa org/lwjgl/lwjgl-opengl/%LWJGL% lwjgl-opengl-%LWJGL%-%NAT%.jar
if errorlevel 1 goto erro
rem lwjgl_util (LWJGL 2) so para as classes Matrix4f/Vector3f usadas no Main3D e nos objetos
call :baixa org/lwjgl/lwjgl/lwjgl_util/2.9.1 lwjgl_util-2.9.1.jar
if errorlevel 1 goto erro

rem o explode.gif original fica na pasta acima; copia para ca se faltar
if not exist "explode.gif" if exist "..\explode.gif" copy /y "..\explode.gif" "explode.gif" >nul

if not exist target\classes mkdir target\classes

echo == Compilando ==
rem -sourcepath faz o javac compilar tambem as classes usadas pelo Main3D (pacotes obj, Model, shaders, util)
"%JDKBIN%javac" -encoding UTF-8 -nowarn -cp "lib\*" -sourcepath src\main\java -d target\classes src\main\java\main\Main3D.java
if errorlevel 1 goto erro

echo == Executando ==
rem Main3D esta no pacote "main", por isso o nome completo main.Main3D
"%JDKBIN%java" -cp "target\classes;lib\*" main.Main3D
if errorlevel 1 goto erro
goto fim

rem ---- baixa um jar do Maven Central para lib\ (se ainda nao existir) ----
:baixa
if exist "lib\%~2" exit /b 0
set "URL=https://repo1.maven.org/maven2/%~1/%~2"
echo Baixando %~2 ...
curl -fsSL -o "lib\%~2" "%URL%" 2>nul
if errorlevel 1 (
    del "lib\%~2" 2>nul
    powershell -NoProfile -Command "Invoke-WebRequest -UseBasicParsing -Uri '%URL%' -OutFile 'lib\%~2'"
)
if not exist "lib\%~2" (
    echo [ERRO] Nao consegui baixar %URL%
    exit /b 1
)
exit /b 0

:erro
echo.
echo [ERRO] Falha ao compilar ou executar. Veja as mensagens acima.

:fim
pause

@echo off
cd /d "%~dp0"
if not exist tailwindcss-windows-x64.exe (
  echo Falta tailwindcss-windows-x64.exe en esta carpeta. Descargalo de https://github.com/tailwindlabs/tailwindcss/releases
  pause
  exit /b 1
)
tailwindcss-windows-x64.exe -i src\main\resources\tailwind\input.css -o src\main\resources\static\css\app.css --minify
echo.
echo Listo: src\main\resources\static\css\app.css generado.
pause

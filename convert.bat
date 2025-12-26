@echo off
chcp 65001 >nul
echo ========================================
echo 毕业设计论文转换工具
echo ========================================
echo.

REM 检查Pandoc是否安装
pandoc --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 未检测到Pandoc，请先安装Pandoc
    echo.
    echo 安装方法：
    echo 1. 访问 https://github.com/jgm/pandoc/releases
    echo 2. 下载并安装 pandoc-x.x.x-windows-x86_64.msi
    echo 3. 或使用: choco install pandoc
    echo.
    pause
    exit /b 1
)

echo [信息] 检测到Pandoc，开始转换...
echo.

REM 执行转换
pandoc "毕业设计论文.md" -o "毕业设计论文.docx" --toc --toc-depth=3 --number-sections --highlight-style=tango --standalone

if %errorlevel% equ 0 (
    echo.
    echo [成功] 转换完成！
    echo 输出文件: 毕业设计论文.docx
    echo.
) else (
    echo.
    echo [错误] 转换失败，请检查文件是否存在
    echo.
)

pause


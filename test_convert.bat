@echo off
chcp 65001 >nul
cd /d "D:\Vue3\top1"
echo ========================================
echo 测试转换工具
echo ========================================
echo.

echo [信息] 开始转换测试...
echo.

pandoc "毕业设计论文.md" -o "毕业设计论文_测试.docx" --toc --toc-depth=3 --number-sections --highlight-style=tango --standalone

if %errorlevel% equ 0 (
    echo.
    echo [成功] 转换完成！
    echo 输出文件: 毕业设计论文_测试.docx
    echo.
    echo 请打开文件查看效果
) else (
    echo.
    echo [错误] 转换失败
    echo 错误代码: %errorlevel%
)

echo.
pause


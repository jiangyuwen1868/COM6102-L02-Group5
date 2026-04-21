@echo off
setlocal enabledelayedexpansion
chcp 65001
REM 测试配置
set API_BASE=http://localhost:8080/api
set PRODUCT_ID=1
set USER_ID=349
set TOKEN=009d87a0054c43e8b5ee93a4db09f748

REM 测试次数
set TEST_COUNT=100
REM 并发数
set CONCURRENT=20

REM 颜色设置
echo.
echo [92m========================================[0m
echo 秒杀系统保护机制测试脚本
echo 测试目标: %API_BASE%/seckill/%PRODUCT_ID%
echo 测试用户: %USER_ID%
echo 测试次数: %TEST_COUNT%
echo 并发数: %CONCURRENT%
echo [92m========================================[0m
echo.

REM 检查token是否设置
if "%TOKEN%"=="YOUR_AUTH_TOKEN_HERE" (
    echo [91m错误: 请先设置有效的认证token[0m
    echo 请在脚本中设置 TOKEN 变量
    pause
    exit /b 1
)

REM 创建临时目录
if not exist "temp" mkdir "temp"

REM 清理旧文件
del /q "temp\*.txt" 2>nul
type nul > test_results.txt

echo [92m开始测试限流、防刷和熔断机制...[0m
echo.

REM 并发测试
for /l %%i in (1,1,%CONCURRENT%) do (
    start /b cmd /c "curl -s -X POST "%API_BASE%/seckill/%PRODUCT_ID%" -H "Authorization: Bearer %TOKEN%" -H "X-Real-IP: 192.168.1.%%i" -d "requestId=req_%%i_%time:~0,2%%time:~3,2%%time:~6,2%" > "temp\result_%%i.txt" && echo ."
)

REM 等待所有请求完成
echo 等待所有请求完成...
timeout /t 10 >nul

REM 合并结果
echo 合并测试结果...
for /l %%i in (1,1,%CONCURRENT%) do (
    if exist "temp\result_%%i.txt" (
        type "temp\result_%%i.txt" >> test_results.txt
    )
)

echo.
echo [92m测试完成，正在分析结果...[0m
echo.

REM 分析结果
echo [93m测试结果分析:[0m
echo [92m========================================[0m

REM 统计成功、失败和限流次数
type test_results.txt | find "秒杀成功" /c > success.txt
type test_results.txt | find "参与人数过多" /c > rate_limit.txt
type test_results.txt | find "请求过于频繁" /c > anti_brush.txt
type test_results.txt | find "系统繁忙" /c > circuit_breaker.txt
type test_results.txt | find "已参与过" /c > already_participated.txt
type test_results.txt | find "请勿重复提交" /c > duplicate_request.txt
type test_results.txt | find "系统异常" /c > system_error.txt

echo 成功次数: [92m
set /p success=<success.txt
echo %success% [0m
echo 限流次数: [93m
set /p rate_limit=<rate_limit.txt
echo %rate_limit% [0m
echo 防刷次数: [91m
set /p anti_brush=<anti_brush.txt
echo %anti_brush% [0m
echo 熔断次数: [91m
set /p circuit_breaker=<circuit_breaker.txt
echo %circuit_breaker% [0m
echo 已参与次数: [93m
set /p already_participated=<already_participated.txt
echo %already_participated% [0m
echo 重复请求: [91m
set /p duplicate_request=<duplicate_request.txt
echo %duplicate_request% [0m
echo 系统异常: [91m
set /p system_error=<system_error.txt
echo %system_error% [0m
echo [92m========================================[0m

REM 显示部分响应示例
echo.
echo [93m响应示例:[0m
echo.
set "lineCount=0"
for /f "tokens=*" %%a in (test_results.txt) do (
    if !lineCount! lss 5 (
        echo %%a
        set /a lineCount=!lineCount!+1
    )
)

REM 清理临时文件
del /q "temp\*.txt" 2>nul
del test_results.txt success.txt rate_limit.txt anti_brush.txt circuit_breaker.txt already_participated.txt duplicate_request.txt system_error.txt 2>nul
rd "temp" 2>nul

echo.
echo [92m测试完成！[0m
echo 请检查系统日志以获取更详细的信息。
echo.
pause
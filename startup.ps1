# 快速启动脚本

# 0. 环境配置 (自动检测 IDEA 下载的 JDK 17)
$ideaJdkPath = "$env:USERPROFILE\.jdks\corretto-17.0.11"
if (Test-Path $ideaJdkPath) {
    Write-Host "检测到 IDEA JDK 17: $ideaJdkPath" -ForegroundColor Green
    $env:JAVA_HOME = $ideaJdkPath
    $env:Path = "$ideaJdkPath\bin;$env:Path"
}

# 检查 Java 版本
$javaVer = java -version 2>&1 | Out-String
if ($javaVer -notmatch "17\.|21\.") {
    Write-Host "警告: 当前 Java 版本可能不兼容 ($javaVer)" -ForegroundColor Yellow
    Write-Host "建议使用 JDK 17+" -ForegroundColor Yellow
}

# 1. 构建项目
Write-Host "1. 开始构建项目..." -ForegroundColor Cyan
mvn clean install -DskipTests
if ($LASTEXITCODE -ne 0) {
    Write-Host "构建失败，请检查 Maven 环境或错误日志" -ForegroundColor Red
    exit
}
Write-Host "构建成功！" -ForegroundColor Green

# 2. 定义服务列表
$services = @(
    @{ Name = "gateway-service"; Port = 8080 },
    @{ Name = "user-service"; Port = 8081 },
    @{ Name = "permission-service"; Port = 8082 },
    @{ Name = "auth-service"; Port = 8083 },
    @{ Name = "notification-service"; Port = 8084 }
)

# 3. 启动服务
Write-Host "2. 准备启动服务..." -ForegroundColor Cyan
Write-Host "注意：请确保 Nacos(8848) 和 Redis(6379) 已经启动！" -ForegroundColor Yellow
Start-Sleep -Seconds 3

foreach ($service in $services) {
    $name = $service.Name
    # 查找 jar 包
    $jarFile = Get-ChildItem "$name/target/*.jar" | Where-Object { $_.Name -notlike "*sources*" } | Select-Object -First 1
    
    if ($jarFile) {
        Write-Host "正在启动 $name ..." -ForegroundColor Green
        # 使用新窗口启动 Java 进程
        Start-Process java -ArgumentList "-Dfile.encoding=UTF-8", "-jar", $jarFile.FullName, "--spring.profiles.active=dev" -WorkingDirectory "$PWD\$name"
        
        # 稍微等待一下，避免瞬间并发过高
        Start-Sleep -Seconds 2
    } else {
        Write-Host "未找到 $name 的 Jar 包，跳过启动" -ForegroundColor Red
    }
}

Write-Host "所有服务启动命令已发送。" -ForegroundColor Cyan
Write-Host "请检查弹出的 5 个窗口以确认服务启动状态。" -ForegroundColor Cyan
Write-Host "Nacos 控制台: http://localhost:8848/nacos"
Write-Host "Gateway 地址: http://localhost:8080"

$modules = @("user-service", "permission-service", "auth-service", "notification-service")

# Common replacements
foreach ($module in $modules) {
    $files = Get-ChildItem -Path "$module/src/main/java" -Recurse -Filter "*.java"
    foreach ($file in $files) {
        $content = Get-Content $file.FullName -Raw
        # Update Entity Imports
        $content = $content -replace "import com.dseven.rolepermission.entity.", "import com.dseven.rolepermission.common.entity."
        # Update Common Utils/Result/Exception imports (if package changed)
        # Result: package com.dseven.rolepermission.common.result (No change)
        # Exception: BizException changed to common.exception
        $content = $content -replace "import com.dseven.rolepermission.biz.mail.exception.BizException;", "import com.dseven.rolepermission.common.exception.BizException;"
        # JwtUtil: changed to common.utils
        $content = $content -replace "import com.dseven.rolepermission.utils.JwtUtil;", "import com.dseven.rolepermission.common.utils.JwtUtil;"
        
        Set-Content $file.FullName $content
    }
}

# User Service specific
$userFiles = Get-ChildItem -Path "user-service/src/main/java" -Recurse -Filter "*.java"
foreach ($file in $userFiles) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace "package com.dseven.rolepermission.service;", "package com.dseven.rolepermission.user.service;"
    $content = $content -replace "package com.dseven.rolepermission.service.impl;", "package com.dseven.rolepermission.user.service.impl;"
    $content = $content -replace "package com.dseven.rolepermission.mapper;", "package com.dseven.rolepermission.user.mapper;"
    # Fix internal imports
    $content = $content -replace "import com.dseven.rolepermission.mapper.SysUser", "import com.dseven.rolepermission.user.mapper.SysUser"
    $content = $content -replace "import com.dseven.rolepermission.service.SysUser", "import com.dseven.rolepermission.user.service.SysUser"
    Set-Content $file.FullName $content
}

# Permission Service specific
$permFiles = Get-ChildItem -Path "permission-service/src/main/java" -Recurse -Filter "*.java"
foreach ($file in $permFiles) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace "package com.dseven.rolepermission.service;", "package com.dseven.rolepermission.permission.service;"
    $content = $content -replace "package com.dseven.rolepermission.service.impl;", "package com.dseven.rolepermission.permission.service.impl;"
    $content = $content -replace "package com.dseven.rolepermission.mapper;", "package com.dseven.rolepermission.permission.mapper;"
    # Fix internal imports
    $content = $content -replace "import com.dseven.rolepermission.mapper.SysRole", "import com.dseven.rolepermission.permission.mapper.SysRole"
    $content = $content -replace "import com.dseven.rolepermission.service.SysRole", "import com.dseven.rolepermission.permission.service.SysRole"
     $content = $content -replace "import com.dseven.rolepermission.mapper.SysPermission", "import com.dseven.rolepermission.permission.mapper.SysPermission"
    $content = $content -replace "import com.dseven.rolepermission.service.SysPermission", "import com.dseven.rolepermission.permission.service.SysPermission"
    Set-Content $file.FullName $content
}

# Notification Service specific
$notifFiles = Get-ChildItem -Path "notification-service/src/main/java" -Recurse -Filter "*.java"
foreach ($file in $notifFiles) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace "package com.dseven.rolepermission.biz.mail", "package com.dseven.rolepermission.notification"
    # This might need refinement for subpackages
    Set-Content $file.FullName $content
}

# Auth Service specific
$authFiles = Get-ChildItem -Path "auth-service/src/main/java" -Recurse -Filter "*.java"
foreach ($file in $authFiles) {
    $content = Get-Content $file.FullName -Raw
    $content = $content -replace "package com.dseven.rolepermission.sso", "package com.dseven.rolepermission.auth"
    $content = $content -replace "package com.dseven.rolepermission.service;", "package com.dseven.rolepermission.auth.service;"
     $content = $content -replace "package com.dseven.rolepermission.service.impl;", "package com.dseven.rolepermission.auth.service.impl;"
    Set-Content $file.FullName $content
}

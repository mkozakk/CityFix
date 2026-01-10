# CityFix Traffic Generator - Clean Version

$baseUrl = "http://localhost:8080"



# Phase 1: User Registration
for ($i=1; $i -le 5; $i++) {
    $body = @{
        username = "testuser$i"
        email = "testuser$i@cityfix.com"
        password = "SecurePass123!"
    } | ConvertTo-Json

    try {
        Invoke-WebRequest -Uri "$baseUrl/api/users/register" `
            -Method POST `
            -ContentType "application/json" `
            -Body $body `
            -ErrorAction Stop | Out-Null
    } catch {
        if ($_.Exception.Response.StatusCode -ne 409) {
            # Ignore 409 (user exists), other errors increment fail counter
            continue
        }
    }
    Start-Sleep -Milliseconds 200
}

# Phase 2: Generate Traffic
$totalRequests = 0
$successRequests = 0
$failedRequests = 0

for ($i=1; $i -le 5; $i++) {
    $loginBody = @{
        username = "testuser$i"
        password = "SecurePass123!"
    } | ConvertTo-Json

    try {
        $response = Invoke-WebRequest -Uri "$baseUrl/api/users/login" `
            -Method POST `
            -ContentType "application/json" `
            -Body $loginBody `
            -SessionVariable session `
            -ErrorAction Stop

        $totalRequests++
        $successRequests++

        # GET /me requests
        for ($j=1; $j -le 20; $j++) {
            try {
                Invoke-WebRequest -Uri "$baseUrl/api/users/me" `
                    -Method GET `
                    -WebSession $session `
                    -ErrorAction Stop | Out-Null
                $successRequests++
            } catch {
                $failedRequests++
            }
            $totalRequests++
            Start-Sleep -Milliseconds 50
        }

        # GET /reports requests
        for ($k=1; $k -le 5; $k++) {
            try {
                Invoke-WebRequest -Uri "$baseUrl/api/reports" `
                    -Method GET `
                    -WebSession $session `
                    -ErrorAction SilentlyContinue | Out-Null
            } catch {}
            $totalRequests++
            Start-Sleep -Milliseconds 50
        }

    } catch {
        $failedRequests++
    }
}

# Phase 3: Mixed Traffic Pattern
for ($round=1; $round -le 3; $round++) {

    # Authorized requests
    $loginBody = @{
        username = "testuser1"
        password = "SecurePass123!"
    } | ConvertTo-Json

    try {
        Invoke-WebRequest -Uri "$baseUrl/api/users/login" `
            -Method POST `
            -ContentType "application/json" `
            -Body $loginBody `
            -SessionVariable authSession `
            -ErrorAction SilentlyContinue | Out-Null

        for ($i=1; $i -le 10; $i++) {
            Invoke-WebRequest -Uri "$baseUrl/api/users/me" `
                -Method GET `
                -WebSession $authSession `
                -ErrorAction SilentlyContinue | Out-Null
            $totalRequests++
            Start-Sleep -Milliseconds 30
        }
    } catch {}

    # Unauthorized requests
    for ($i=1; $i -le 5; $i++) {
        try {
            Invoke-WebRequest -Uri "$baseUrl/api/users/me" `
                -Method GET `
                -ErrorAction SilentlyContinue | Out-Null
        } catch {}
        $totalRequests++
        Start-Sleep -Milliseconds 30
    }

    # Not Found requests
    for ($i=1; $i -le 3; $i++) {
        try {
            Invoke-WebRequest -Uri "$baseUrl/api/users/notfound$i" `
                -Method GET `
                -ErrorAction SilentlyContinue | Out-Null
        } catch {}
        $totalRequests++
        Start-Sleep -Milliseconds 30
    }
}

# Summary counters (optional)
$summary = [PSCustomObject]@{
    TotalRequests  = $totalRequests
    Successful     = $successRequests
    FailedOrOther  = $failedRequests
}

$summary

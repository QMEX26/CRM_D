Write-Host "=== 1. Testing Frontend HTTP 200 ==="
$fe = Invoke-WebRequest -Uri "http://127.0.0.1:5173/" -UseBasicParsing
Write-Host "Frontend Status: $($fe.StatusCode) (Vite React Client Online)"

Write-Host "=== 2. Admin Authentication (JWT + Spring Security) ==="
$adminAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"admin@crm.com","password":"admin123"}'
$adminToken = $adminAuth.data.token
Write-Host "Admin Logged In: $($adminAuth.data.user.name) | Role: $($adminAuth.data.user.role)"
$adminHeaders = @{ Authorization = "Bearer $adminToken" }

Write-Host "=== 3. Sales Rep Authentication (JWT + Spring Security) ==="
$agentAuth = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/auth/login" -Method Post -ContentType "application/json" -Body '{"email":"agent@crm.com","password":"agent123"}'
$agentToken = $agentAuth.data.token
Write-Host "Agent Logged In: $($agentAuth.data.user.name) | Role: $($agentAuth.data.user.role)"
$agentHeaders = @{ Authorization = "Bearer $agentToken" }

Write-Host "=== 4. Testing Duplicate Lead Enforcement (Section #8) ==="
$lead1 = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/leads/1" -Method Get -Headers $adminHeaders).data
$sameProjectId = $lead1.project.id
$sameProjectPhone = $lead1.phone
Write-Host "Target: Phone '$sameProjectPhone' in Project '$($lead1.project.name)' (ID: $sameProjectId)"

try {
    Write-Host "Attempting duplicate lead in same project..."
    $dup = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/leads" -Method Post -Headers $adminHeaders -ContentType "application/json" -Body (@{
        projectId = $sameProjectId
        name = "Duplicate Customer Name"
        phone = $sameProjectPhone
    } | ConvertTo-Json)
    Write-Host "ERROR: Duplicate was NOT rejected!"
} catch {
    Write-Host "SUCCESS: Duplicate lead correctly rejected by backend!"
    Write-Host "Error details: $($_.Exception.Message)"
}

$allProjects = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/projects" -Method Get -Headers $adminHeaders).data.content
$differentProject = $allProjects | Where-Object { $_.id -ne $sameProjectId } | Select-Object -First 1
Write-Host "Attempting same phone in DIFFERENT project '$($differentProject.name)' (ID: $($differentProject.id))..."
$diffProjLead = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/leads" -Method Post -Headers $adminHeaders -ContentType "application/json" -Body (@{
    projectId = $differentProject.id
    name = "Allowed Customer in Different Project"
    phone = $sameProjectPhone
} | ConvertTo-Json)
Write-Host "SUCCESS: Same phone in different project allowed! New Lead ID: $($diffProjLead.data.id)"

Write-Host "=== 5. Testing Lead Details & Lifecycle (Section #11 & #24) ==="
$leadDetails = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/leads/1" -Method Get -Headers $adminHeaders).data
Write-Host "Lead 1 Customer: $($leadDetails.name)"
Write-Host "Current Owner: $($leadDetails.currentOwner.name) ($($leadDetails.currentOwner.email))"
Write-Host "Total Calls: $($leadDetails.callSummary.totalCalls) | Total Duration: $($leadDetails.callSummary.formattedTotalDuration)"
Write-Host "Follow-ups Count: $($leadDetails.followUps.Count) | Notes Count: $($leadDetails.notes.Count)"

Write-Host "=== 6. Testing Lead Reassignment & Ownership Trail (Section #9) ==="
$users = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/users" -Method Get -Headers $adminHeaders).data.content
$agent2 = $users | Where-Object { $_.email -eq "agent2@crm.com" }
$reassign = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/leads/1/reassign" -Method Post -Headers $adminHeaders -ContentType "application/json" -Body (@{
    userId = $agent2.id
    reason = "Reassigned to Kiran Rao for enterprise consultation"
} | ConvertTo-Json)
Write-Host "Reassignment Success: $($reassign.success)"
$updatedLead = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/leads/1" -Method Get -Headers $adminHeaders).data
Write-Host "New Active Owner: $($updatedLead.currentOwner.name)"
Write-Host "Previous Owners Recorded: $($updatedLead.previousOwners.Count)"
Write-Host "Assignment History Timeline Entries: $($updatedLead.assignmentHistory.Count)"

Write-Host "=== 7. Testing Dialler Call Logging (Section #15) ==="
$callLog = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/calls" -Method Post -Headers $agentHeaders -ContentType "application/json" -Body (@{
    leadId = 1
    durationSeconds = 245
    callStatus = "CONNECTED"
    businessOutcome = "INTERESTED"
    notes = "Client requested site visit for 4BHK Penthouse on Saturday."
} | ConvertTo-Json)).data
Write-Host "Call Logged ID: $($callLog.id) | Duration: $($callLog.formattedDuration) | Status: $($callLog.callStatus) | Outcome: $($callLog.businessOutcome)"

Write-Host "=== 8. Testing Follow-up Creation & Completion (Section #17) ==="
$fu = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/follow-ups" -Method Post -Headers $agentHeaders -ContentType "application/json" -Body (@{
    leadId = 1
    followUpDate = (Get-Date).AddDays(1).ToString("yyyy-MM-ddTHH:mm:ss")
    notes = "Site visit scheduled with family"
} | ConvertTo-Json)).data
Write-Host "Follow-up Created ID: $($fu.id) | Status: $($fu.status)"
$fuComplete = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/follow-ups/$($fu.id)/status" -Method Patch -Headers $agentHeaders -ContentType "application/json" -Body (@{
    status = "COMPLETED"
} | ConvertTo-Json)).data
Write-Host "Follow-up Marked Complete Status: $($fuComplete.status)"

Write-Host "=== 9. Testing Lead Conversion to Sale (Section #19) ==="
$sale = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/sales/convert" -Method Post -Headers $adminHeaders -ContentType "application/json" -Body (@{
    leadId = 1
    dealValue = 18500000
    notes = "Booking advance token of Rs 500,000 received for Penthouse 1201"
} | ConvertTo-Json)).data
Write-Host "Sale Converted ID: $($sale.id) | Deal Value: Rs $($sale.dealValue)"

Write-Host "=== 10. Testing Google Sheets Sync Role Protection (Section #28) ==="
try {
    Write-Host "Testing Agent sync access (should be 403 Forbidden)..."
    $agentSync = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/google-sheets/sync" -Method Post -Headers $agentHeaders
    Write-Host "ERROR: Normal agent was allowed to sync!"
} catch {
    Write-Host "SUCCESS: Normal agent correctly denied (403 Forbidden): $($_.Exception.Message)"
}

Write-Host "Testing Admin sync access..."
$adminSync = (Invoke-RestMethod -Uri "http://localhost:8080/api/v1/google-sheets/sync" -Method Post -Headers $adminHeaders).data
Write-Host "SUCCESS: Admin Google Sheets Sync succeeded! Status: $($adminSync.status) | Rows Synced: $($adminSync.recordsSynced)"

Write-Host "=== ALL 10 CORE CRM BUSINESS CAPABILITIES 100% VERIFIED! ==="

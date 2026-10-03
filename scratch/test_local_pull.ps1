$headers = @{ "X-Sync-Secret" = "AKfycbyQFNJjPe8hL6Zfmeatdtizd77E0mKSOikhOW6L9Ye1gdrU2ZFvrJpD0jfhRROUL_JL" }
$res = Invoke-RestMethod -Uri "http://localhost:8080/api/v1/google-sheets/pull" -Method Post -Headers $headers -ContentType "application/json"
Write-Host "Success:" $res.success
Write-Host "Total Records:" $res.data.totalRecords
foreach ($t in $res.data.tables.PSObject.Properties) {
    Write-Host "Table: $($t.Name) -> $($t.Value.Count) records"
}

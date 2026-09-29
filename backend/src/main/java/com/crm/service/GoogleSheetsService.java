package com.crm.service;

import com.crm.dto.request.GoogleSheetsPushRequest;
import com.crm.dto.response.GoogleSheetsPullResponse;
import com.crm.dto.response.GoogleSheetsPushResponse;
import com.crm.dto.response.GoogleSheetsSyncResponse;
import com.crm.model.GoogleSheetsSyncLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GoogleSheetsService {
    GoogleSheetsSyncResponse triggerSync(Long adminUserId);
    GoogleSheetsPullResponse pullDatabaseSnapshot(Long adminUserId);
    GoogleSheetsPushResponse pushDatabaseSnapshot(GoogleSheetsPushRequest request, Long adminUserId);
    Page<GoogleSheetsSyncLog> getSyncHistory(Pageable pageable);
    GoogleSheetsSyncResponse getLatestSyncStatus();
}

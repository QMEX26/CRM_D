package com.crm.service;

import com.crm.dto.request.NoteCreateRequest;
import com.crm.dto.response.NoteResponse;
import com.crm.model.Note;

import java.util.List;

public interface NoteService {
    NoteResponse addNote(Long leadId, NoteCreateRequest request, Long userId, boolean isAdmin);
    List<Note> getNotesByLead(Long leadId);
    List<Note> getNotesByLead(Long leadId, Long userId, boolean isAdmin);
}

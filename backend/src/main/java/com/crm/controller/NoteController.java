package com.crm.controller;

import com.crm.dto.request.NoteCreateRequest;
import com.crm.dto.response.ApiResponse;
import com.crm.dto.response.NoteResponse;
import com.crm.mapper.NoteMapper;
import com.crm.model.Note;
import com.crm.security.CurrentUser;
import com.crm.security.UserPrincipal;
import com.crm.service.NoteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leads/{leadId}/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final NoteMapper noteMapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoteResponse>>> getNotes(
            @PathVariable Long leadId,
            @CurrentUser UserPrincipal principal) {
        List<Note> notes = noteService.getNotesByLead(leadId, principal.getId(), principal.isAdmin());
        List<NoteResponse> responses = notes.stream().map(noteMapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(responses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoteResponse>> addNote(
            @PathVariable Long leadId,
            @Valid @RequestBody NoteCreateRequest request,
            @CurrentUser UserPrincipal principal) {

        NoteResponse response = noteService.addNote(leadId, request, principal.getId(), principal.isAdmin());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Note added successfully", response));
    }
}

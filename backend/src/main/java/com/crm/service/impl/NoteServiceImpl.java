package com.crm.service.impl;

import com.crm.dto.request.NoteCreateRequest;
import com.crm.dto.response.NoteResponse;
import com.crm.exception.ForbiddenException;
import com.crm.exception.ResourceNotFoundException;
import com.crm.mapper.NoteMapper;
import com.crm.model.Lead;
import com.crm.model.Note;
import com.crm.model.User;
import com.crm.repository.LeadRepository;
import com.crm.repository.NoteRepository;
import com.crm.repository.UserRepository;
import com.crm.service.AuditService;
import com.crm.service.LeadAssignmentService;
import com.crm.service.NoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final LeadRepository leadRepository;
    private final UserRepository userRepository;
    private final LeadAssignmentService leadAssignmentService;
    private final NoteMapper noteMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public NoteResponse addNote(Long leadId, NoteCreateRequest request, Long userId, boolean isAdmin) {
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with id: " + leadId));

        if (!leadAssignmentService.isUserAllowedToAccessLead(leadId, userId, isAdmin)) {
            throw new ForbiddenException("Access denied: You are not authorized to add notes to this lead");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Note note = Note.builder()
                .lead(lead)
                .user(user)
                .content(request.getContent())
                .build();

        Note saved = noteRepository.save(note);

        auditService.logAction(userId, "Note", saved.getId(), "ADD_NOTE", null,
                "Note added to Lead: " + lead.getName());

        return noteMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Note> getNotesByLead(Long leadId) {
        if (!leadRepository.existsById(leadId)) {
            throw new ResourceNotFoundException("Lead not found with id: " + leadId);
        }
        return noteRepository.findByLeadIdOrderByCreatedAtDesc(leadId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Note> getNotesByLead(Long leadId, Long userId, boolean isAdmin) {
        if (!leadRepository.existsById(leadId)) {
            throw new ResourceNotFoundException("Lead not found with id: " + leadId);
        }
        if (!leadAssignmentService.isUserAllowedToAccessLead(leadId, userId, isAdmin)) {
            throw new ForbiddenException("Access denied: You are not authorized to view notes for this lead");
        }
        return noteRepository.findByLeadIdOrderByCreatedAtDesc(leadId);
    }
}

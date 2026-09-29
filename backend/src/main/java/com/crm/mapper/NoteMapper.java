package com.crm.mapper;

import com.crm.dto.response.NoteResponse;
import com.crm.model.Note;
import org.springframework.stereotype.Component;

@Component
public class NoteMapper {

    public NoteResponse toResponse(Note note) {
        if (note == null) return null;

        return NoteResponse.builder()
                .id(note.getId())
                .leadId(note.getLead() != null ? note.getLead().getId() : null)
                .userId(note.getUser() != null ? note.getUser().getId() : null)
                .userName(note.getUser() != null ? note.getUser().getName() : null)
                .content(note.getContent())
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}

package com.saasweb.core.controller;

import com.saasweb.core.model.CreditNote;
import com.saasweb.core.dto.CreditNoteDtos;
import com.saasweb.core.service.CreditNoteService;

import com.saasweb.core.dto.CreditNoteDtos.CreditNoteRequest;
import com.saasweb.core.dto.CreditNoteDtos.CreditNoteResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders/{orderId}/credit-notes")
@PreAuthorize("hasAuthority('ORDERS_MANAGE')")
public class CreditNoteController {

    private final CreditNoteService service;

    public CreditNoteController(CreditNoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<CreditNoteResponse> list(@PathVariable String orderId) {
        return service.findByOrder(orderId).stream().map(CreditNoteResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<CreditNoteResponse> emit(@PathVariable String orderId,
            @Valid @RequestBody CreditNoteRequest req, Authentication auth) {
        CreditNote cn = service.emit(orderId, req.amount(), req.reason(), auth.getName());
        return ResponseEntity.status(201).body(CreditNoteResponse.from(cn));
    }
}

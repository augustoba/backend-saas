package com.saasweb.ecommerce.controller;

import com.saasweb.ecommerce.model.Shift;

import com.saasweb.core.PageResponse;
import com.saasweb.ecommerce.dto.ShiftDtos.ShiftResponse;
import com.saasweb.ecommerce.service.ShiftService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/shifts")
@PreAuthorize("hasAuthority('SHIFTS_MANAGE')")
public class ShiftController {

    private final ShiftService service;

    public ShiftController(ShiftService service) {
        this.service = service;
    }

    @GetMapping("/current")
    public ShiftResponse current(Authentication auth) {
        return service.currentOpen(auth.getName()).map(ShiftResponse::from).orElse(null);
    }

    @PostMapping("/open")
    public ShiftResponse open(Authentication auth) {
        return ShiftResponse.from(service.open(auth.getName()));
    }

    @PostMapping("/{id}/close")
    public ShiftResponse close(@PathVariable String id, Authentication auth) {
        return ShiftResponse.from(service.close(id, auth.getName()));
    }

    @GetMapping
    public PageResponse<ShiftResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String userDni) {
        int capped = Math.min(Math.max(size, 1), 100);
        Page<com.saasweb.ecommerce.model.Shift> result =
                service.list(userDni, PageRequest.of(Math.max(page, 0), capped));
        return PageResponse.of(result, result.map(ShiftResponse::from).getContent());
    }
}

package com.stockpulse.controller;

import com.stockpulse.AdvisorRouter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@RestController
@RequestMapping("/api/advisor")
@RequiredArgsConstructor
public class AdvisorController {
    private final AdvisorRouter router;
    @GetMapping("/mode")
    public Map<String,String> getMode() { return Map.of("mode", router.getMode()); }
    @PostMapping("/mode")
    public Map<String,String> setMode(@RequestBody Map<String,String> body) {
        try { router.setMode(body.get("mode")); }
        catch (RuntimeException e) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage()); }
        return getMode();
    }
}

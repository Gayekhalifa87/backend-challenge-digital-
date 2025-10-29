package com.challenge_digital.cila_bokk.controller;

import com.challenge_digital.cila_bokk.dto.CreateProjetRequest;
import com.challenge_digital.cila_bokk.dto.ProjetDTO;
import com.challenge_digital.cila_bokk.model.StatutProjet;
import com.challenge_digital.cila_bokk.service.ProjetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projets")
@RequiredArgsConstructor
public class ProjetController {

    private final ProjetService projetService;

    @GetMapping
    public ResponseEntity<List<ProjetDTO>> getAllProjets() {
        return ResponseEntity.ok(projetService.getAllProjets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjetDTO> getProjetById(@PathVariable Long id) {
        return projetService.getProjetById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/agent/{agentId}")
    public ResponseEntity<List<ProjetDTO>> getProjetsByAgent(@PathVariable Long agentId) {
        return ResponseEntity.ok(projetService.getProjetsByAgent(agentId));
    }

    @GetMapping("/agent/{agentId}/brouillons")
    public ResponseEntity<List<ProjetDTO>> getProjetsBrouillon(@PathVariable Long agentId) {
        return ResponseEntity.ok(projetService.getProjetsBrouillon(agentId));
    }

    @GetMapping("/statut/{statut}")
    public ResponseEntity<List<ProjetDTO>> getProjetsByStatut(@PathVariable StatutProjet statut) {
        return ResponseEntity.ok(projetService.getProjetsByStatut(statut));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getTotalProjets() {
        return ResponseEntity.ok(projetService.getTotalProjets());
    }

    @GetMapping("/count/statut/{statut}")
    public ResponseEntity<Long> countByStatut(@PathVariable StatutProjet statut) {
        return ResponseEntity.ok(projetService.countByStatut(statut));
    }

    @GetMapping("/count/agent/{agentId}/brouillons")
    public ResponseEntity<Long> countBrouillons(@PathVariable Long agentId) {
        return ResponseEntity.ok(projetService.countBrouillons(agentId));
    }

    @PostMapping
    public ResponseEntity<?> createProjet(@RequestBody CreateProjetRequest request) {
        try {
            return ResponseEntity.ok(projetService.createProjet(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProjet(@PathVariable Long id, @RequestBody CreateProjetRequest request) {
        try {
            return ResponseEntity.ok(projetService.updateProjet(id, request));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/soumettre")
    public ResponseEntity<?> soumettreProjet(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(projetService.soumettreProjet(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProjet(@PathVariable Long id) {
        try {
            boolean deleted = projetService.deleteProjet(id);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }
}

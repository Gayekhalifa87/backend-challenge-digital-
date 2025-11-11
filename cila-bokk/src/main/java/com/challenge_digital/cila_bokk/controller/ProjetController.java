package com.challenge_digital.cila_bokk.controller;

import com.challenge_digital.cila_bokk.dto.CreateProjetRequest;
import com.challenge_digital.cila_bokk.dto.ProjetDTO;
import com.challenge_digital.cila_bokk.model.StatutProjet;
import com.challenge_digital.cila_bokk.service.ProjetService;
import com.challenge_digital.cila_bokk.service.external.AgentService;
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
    private final AgentService agentService;

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

    /**
     * ✅ Nouveau: Valider une équipe avant soumission
     * Permet au frontend de vérifier que les matricules sont valides
     */
    @PostMapping("/validate-equipe")
    public ResponseEntity<?> validateEquipe(@RequestBody List<String> matricules) {
        try {
            Map<String, Object> validation = agentService.validateEquipe(matricules);
            return ResponseEntity.ok(validation);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur lors de la validation: " + e.getMessage()));
        }
    }

    /**
     * ✅ Nouveau: Rechercher des agents par matricules
     * Utile pour l'autocomplétion dans le formulaire
     */
    @PostMapping("/search-agents")
    public ResponseEntity<?> searchAgentsByMatricules(@RequestBody List<String> matricules) {
        try {
            List<Map<String, Object>> agents = agentService.getAgentsByMatricules(matricules);
            return ResponseEntity.ok(agents);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur lors de la recherche: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createProjet(@RequestBody CreateProjetRequest request) {
        try {
            // ✅ Récupérer l'agent connecté depuis Keycloak
            Map<String, Object> connectedAgent = agentService.getConnectedAgentDetails();

            if (connectedAgent == null || connectedAgent.containsKey("message")) {
                return ResponseEntity.status(401).body(Map.of(
                        "message", "Utilisateur non authentifié ou introuvable"
                ));
            }

            // ✅ Injecter l'ID de l'agent connecté dans la requête
            Long idAgent = Long.valueOf(String.valueOf(connectedAgent.get("id")));
            request.setIdAgentSoumission(idAgent);

            // ✅ Créer le projet avec cet agent
            ProjetDTO created = projetService.createProjet(request);

            // ✅ Réponse enrichie avec infos agent + projet créé
            return ResponseEntity.ok(Map.of(
                    "message", "Projet créé avec succès",
                    "agent", connectedAgent,
                    "projet", created
            ));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> updateProjet(
            @PathVariable Long id,
            @RequestBody CreateProjetRequest request
    ) {
        try {
            ProjetDTO updated = projetService.updateProjet(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    @PostMapping("/{id}/soumettre")
    public ResponseEntity<?> soumettreProjet(@PathVariable Long id) {
        try {
            ProjetDTO soumis = projetService.soumettreProjet(id);
            return ResponseEntity.ok(soumis);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProjet(@PathVariable Long id) {
        try {
            boolean deleted = projetService.deleteProjet(id);
            return deleted ?
                    ResponseEntity.noContent().build() :
                    ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur: " + e.getMessage()));
        }
    }

    /**
     * ✅ Endpoint pour récupérer les projets de l'agent connecté avec infos complètes
     */
    @GetMapping("/me")
    public ResponseEntity<?> getMesProjets() {
        try {
            Map<String, Object> connectedAgent = agentService.getConnectedAgentDetails();

            if (connectedAgent.containsKey("message")) {
                return ResponseEntity.status(404).body(connectedAgent);
            }

            Long agentId = Long.valueOf(String.valueOf(connectedAgent.get("id")));
            List<ProjetDTO> projets = projetService.getProjetsByAgent(agentId);

            return ResponseEntity.ok(Map.of(
                    "agent", connectedAgent,
                    "projets", projets
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Map.of("message", "Erreur lors de la récupération des projets: " + e.getMessage()));
        }
    }
}
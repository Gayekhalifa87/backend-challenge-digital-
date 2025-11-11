//package com.challenge_digital.cila_bokk.controller;
//
//import com.challenge_digital.cila_bokk.dto.ValidationDTO;
//import com.challenge_digital.cila_bokk.model.NiveauValidation;
//import com.challenge_digital.cila_bokk.service.ValidationService;
//import lombok.Getter;
//import lombok.RequiredArgsConstructor;
//import lombok.Setter;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/validations")
//@RequiredArgsConstructor
//public class ValidationController {
//
//    private final ValidationService validationService;
//
//    @GetMapping("/projet/{projetId}")
//    public ResponseEntity<List<ValidationDTO>> getValidationsByProjet(@PathVariable Long projetId) {
//        return ResponseEntity.ok(validationService.getValidationsByProjet(projetId));
//    }
//
//    @GetMapping("/historique/{projetId}")
//    public ResponseEntity<List<ValidationDTO>> getHistoriqueValidations(@PathVariable Long projetId) {
//        return ResponseEntity.ok(validationService.getHistoriqueValidations(projetId));
//    }
//
//    @PostMapping("/valider")
//    public ResponseEntity<?> validerProjet(@RequestBody ValidationRequest request) {
//        try {
//            ValidationDTO validation = validationService.validerProjet(
//                    request.getProjetId(),
//                    request.getNiveau(),
//                    request.getIdAgentValidateur(),
//                    request.getCommentaire()
//            );
//            return ResponseEntity.ok(validation);
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("message", "Erreur: " + e.getMessage()));
//        }
//    }
//
//    @PostMapping("/rejeter")
//    public ResponseEntity<?> rejeterProjet(@RequestBody ValidationRequest request) {
//        try {
//            ValidationDTO validation = validationService.rejeterProjet(
//                    request.getProjetId(),
//                    request.getNiveau(),
//                    request.getIdAgentValidateur(),
//                    request.getCommentaire()
//            );
//            return ResponseEntity.ok(validation);
//        } catch (IllegalStateException e) {
//            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
//        } catch (RuntimeException e) {
//            return ResponseEntity.notFound().build();
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of("message", "Erreur: " + e.getMessage()));
//        }
//    }
//
//    @Getter
//    @Setter
//    public static class ValidationRequest {
//        private Long projetId;
//        private NiveauValidation niveau;
//        private Long idAgentValidateur;
//        private String commentaire;
//    }
//}
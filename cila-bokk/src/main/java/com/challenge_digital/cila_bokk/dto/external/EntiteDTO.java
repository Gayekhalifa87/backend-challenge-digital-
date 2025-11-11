package com.challenge_digital.cila_bokk.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Représente une entité (établissement, direction, etc.)
 * Exemple: Diourbel (code: 5100), Petite Côte (code: 7125)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EntiteDTO {
    private Long id;
    private Boolean active;
    private String code;        // 5100, 7125, DTO, etc.
    private String name;        // Diourbel, Petite Côte, etc.
    private EntiteDTO parent;   // Entité parente (peut être null)
    private TypeEntiteDTO type; // Type (ETABLISSEMENT, DIRECTION, etc.)

    /**
     * Retourne une description complète
     */
    public String getDescription() {
        return String.format("%s (%s) - %s",
                name,
                code,
                type != null ? type.getName() : "N/A"
        );
    }
}
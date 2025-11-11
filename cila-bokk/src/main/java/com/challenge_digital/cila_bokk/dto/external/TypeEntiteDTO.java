package com.challenge_digital.cila_bokk.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Type d'entité (ETABLISSEMENT, DIRECTION, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypeEntiteDTO {
    private Long id;
    private Boolean active;
    private String code;  // ETA, DIR, etc.
    private String name;  // ETABLISSEMENT, DIRECTION, etc.
}
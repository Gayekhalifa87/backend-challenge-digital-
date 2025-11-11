package com.challenge_digital.cila_bokk.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Réponse paginée de l'API Entité
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntitePageDTO {
    private List<EntiteDTO> content;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private int size;
    private int number;
}
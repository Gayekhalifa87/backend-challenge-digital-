package com.challenge_digital.cila_bokk.service.external;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AgentPageResponse {
    private List<AgentApiDto> content;
    private int totalPages;
    private long totalElements;
    private boolean first;
    private boolean last;
    private int size;
    private int number;
}
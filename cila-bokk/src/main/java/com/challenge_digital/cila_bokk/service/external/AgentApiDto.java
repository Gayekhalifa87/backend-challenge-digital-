package com.challenge_digital.cila_bokk.service.external;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AgentApiDto {
    private Long id;
    private String fullName;
    private Integer matricule;
    private String email;
    private String telephone;
    private DirectionDto direction;
    private FonctionDto fonction;
    private RattachementDto rattachement;
    private ChefDto chef;
    private Boolean active;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DirectionDto {
        private Long id;
        private Boolean active;
        private String name;
        private String code;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FonctionDto {
        private Long id;
        private Boolean active;
        private String name;
        private String code;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RattachementDto {
        private Long id;
        private Boolean active;
        private String code;
        private String name;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChefDto {
        private Long id;
        private Integer matricule;
        private String fullName;
        private String email;
        private String telephone;
        private Boolean active;
        private FonctionDto fonction;
        private DirectionDto direction;
        private ChefDto chef;
    }
}
//package com.challenge_digital.cila_bokk.config;
//
//import com.challenge_digital.cila_bokk.dto.ProjetDTO;
//import com.challenge_digital.cila_bokk.dto.ValidationDTO;
//import com.challenge_digital.cila_bokk.service.HistoriqueService;
//import lombok.RequiredArgsConstructor;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.stereotype.Component;
//
///**
// * Aspect AOP pour logger automatiquement les actions importantes
// * Fonctionne pour ProjetService et ValidationService
// */
//@Aspect
//@Component
//@RequiredArgsConstructor
//public class LoggingAspect {
//
//    private final HistoriqueService historiqueService;
//
//    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
//
//    // 🔹 Logger console avant chaque méthode ProjetService ou ValidationService
//    @Before("execution(* com.challenge_digital.cila_bokk.service.ProjetService.*(..)) || " +
//            "execution(* com.challenge_digital.cila_bokk.service.ValidationService.*(..))")
//    public void logBefore(JoinPoint joinPoint) {
//        logger.info("🔵 Entering method: " + joinPoint.getSignature().getName());
//    }
//
//    // 🔹 Logger console après chaque méthode ProjetService ou ValidationService
//    @AfterReturning(pointcut = "execution(* com.challenge_digital.cila_bokk.service.ProjetService.*(..)) || " +
//            "execution(* com.challenge_digital.cila_bokk.service.ValidationService.*(..))",
//            returning = "result")
//    public void logAfter(JoinPoint joinPoint, Object result) {
//        logger.info("✅ Method " + joinPoint.getSignature().getName() + " executed successfully");
//    }
//
//    // 🔹 Logger exception
//    @AfterThrowing(pointcut = "execution(* com.challenge_digital.cila_bokk.service.ProjetService.*(..)) || " +
//            "execution(* com.challenge_digital.cila_bokk.service.ValidationService.*(..))",
//            throwing = "ex")
//    public void logException(JoinPoint joinPoint, Throwable ex) {
//        logger.error("❌ Exception in method: " + joinPoint.getSignature().getName(), ex);
//    }
//
//    // 🔹 Logger dans la base uniquement pour les actions importantes
//    @AfterReturning(pointcut = "execution(* com.challenge_digital.cila_bokk.service.ProjetService.createProjet(..)) || " +
//            "execution(* com.challenge_digital.cila_bokk.service.ProjetService.updateProjet(..)) || " +
//            "execution(* com.challenge_digital.cila_bokk.service.ProjetService.soumettreProjet(..)) || " +
//            "execution(* com.challenge_digital.cila_bokk.service.ProjetService.deleteProjet(..)) || " +
//            "execution(* com.challenge_digital.cila_bokk.service.ValidationService.validerProjet(..)) || " +
//            "execution(* com.challenge_digital.cila_bokk.service.ValidationService.rejeterProjet(..))",
//            returning = "result")
//    public void logActionDatabase(JoinPoint joinPoint, Object result) {
//        String method = joinPoint.getSignature().getName();
//        Object[] args = joinPoint.getArgs();
//
//        try {
//            switch (method) {
//                case "createProjet":
//                case "updateProjet":
//                case "soumettreProjet":
//                    if (result instanceof ProjetDTO projet) {
//                        String actionText = switch (method) {
//                            case "createProjet" -> "Création du projet: " + projet.getTitre();
//                            case "updateProjet" -> "Modification du projet: " + projet.getTitre();
//                            default -> "Soumission du projet: " + projet.getTitre();
//                        };
//                        historiqueService.logAction(actionText, projet.getIdAgentSoumission(), projet.getId());
//                    }
//                    break;
//
//                case "deleteProjet":
//                    if (args.length > 0 && args[0] instanceof Long projetId) {
//                        historiqueService.logAction("Suppression du projet ID: " + projetId, null, projetId);
//                    }
//                    break;
//
//                case "validerProjet":
//                case "rejeterProjet":
//                    if (result instanceof ValidationDTO validation) {
//                        String actionText = method.equals("validerProjet") ?
//                                "Validation niveau " + validation.getNiveau() + " du projet ID: " + validation.getProjetId() :
//                                "Rejet niveau " + validation.getNiveau() + " du projet ID: " + validation.getProjetId();
//                        historiqueService.logAction(actionText, validation.getIdAgentValidateur(), validation.getProjetId());
//                    }
//                    break;
//            }
//        } catch (Exception e) {
//            logger.error("❌ Erreur lors du logging dans la BD", e);
//        }
//    }
//}

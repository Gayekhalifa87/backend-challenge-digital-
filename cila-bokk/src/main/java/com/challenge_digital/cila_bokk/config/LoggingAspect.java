package com.challenge_digital.cila_bokk.config;

import com.challenge_digital.cila_bokk.dto.CreateProjetRequest;
import com.challenge_digital.cila_bokk.dto.ProjetDTO;
import com.challenge_digital.cila_bokk.dto.ValidationDTO;
import com.challenge_digital.cila_bokk.service.HistoriqueService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Aspect AOP pour logger automatiquement les actions importantes
 * S'active sur les méthodes des services ProjetService et ValidationService
 */
@Aspect
@Component
public class LoggingAspect {

    @Autowired
    private HistoriqueService historiqueService;

    /**
     * Logger console AVANT l'exécution des méthodes
     */
    @Before("execution(* com.challenge_digital.cila_bokk.service.ProjetService.*(..)) || " +
            "execution(* com.challenge_digital.cila_bokk.service.ValidationService.*(..))")
    public void logBeforeMethod(JoinPoint joinPoint) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        logger.info("🔵 Entering method: " + joinPoint.getSignature().getName());
    }

    /**
     * Logger console APRÈS l'exécution des méthodes
     */
    @AfterReturning(pointcut = "execution(* com.challenge_digital.cila_bokk.service.ProjetService.*(..)) || " +
            "execution(* com.challenge_digital.cila_bokk.service.ValidationService.*(..))",
            returning = "result")
    public void logAfterMethod(JoinPoint joinPoint, Object result) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        logger.info("✅ Method " + joinPoint.getSignature().getName() + " executed successfully");
    }

    /**
     * Logger console en cas d'EXCEPTION
     */
    @AfterThrowing(pointcut = "execution(* com.challenge_digital.cila_bokk.service.ProjetService.*(..)) || " +
            "execution(* com.challenge_digital.cila_bokk.service.ValidationService.*(..))",
            throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        logger.error("❌ Exception in method: " + joinPoint.getSignature().getName(), ex);
    }

    /**
     * Logger dans la base de données pour les actions IMPORTANTES
     */
    @AfterReturning(
            pointcut = "execution(* com.challenge_digital.cila_bokk.service.ProjetService.createProjet(..)) || " +
                    "execution(* com.challenge_digital.cila_bokk.service.ProjetService.updateProjet(..)) || " +
                    "execution(* com.challenge_digital.cila_bokk.service.ProjetService.soumettreProjet(..)) || " +
                    "execution(* com.challenge_digital.cila_bokk.service.ProjetService.deleteProjet(..)) || " +
                    "execution(* com.challenge_digital.cila_bokk.service.ValidationService.validerProjet(..)) || " +
                    "execution(* com.challenge_digital.cila_bokk.service.ValidationService.rejeterProjet(..))",
            returning = "result")
    public void logActionToDatabase(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        try {
            switch (methodName) {
                case "createProjet":
                    if (result instanceof ProjetDTO) {
                        ProjetDTO projet = (ProjetDTO) result;
                        historiqueService.logAction(
                                "Création du projet: " + projet.getTitre(),
                                projet.getIdAgentSoumission(),
                                projet.getId()
                        );
                    }
                    break;

                case "updateProjet":
                    if (result instanceof ProjetDTO) {
                        ProjetDTO projet = (ProjetDTO) result;
                        historiqueService.logAction(
                                "Modification du projet: " + projet.getTitre(),
                                projet.getIdAgentSoumission(),
                                projet.getId()
                        );
                    }
                    break;

                case "soumettreProjet":
                    if (result instanceof ProjetDTO) {
                        ProjetDTO projet = (ProjetDTO) result;
                        historiqueService.logAction(
                                "Soumission du projet: " + projet.getTitre(),
                                projet.getIdAgentSoumission(),
                                projet.getId()
                        );
                    }
                    break;

                case "deleteProjet":
                    if (args.length > 0 && args[0] instanceof Long) {
                        Long projetId = (Long) args[0];
                        historiqueService.logAction(
                                "Suppression du projet ID: " + projetId,
                                null,
                                projetId
                        );
                    }
                    break;

                case "validerProjet":
                    if (result instanceof ValidationDTO) {
                        ValidationDTO validation = (ValidationDTO) result;
                        historiqueService.logAction(
                                "Validation niveau " + validation.getNiveau() + " du projet ID: " + validation.getProjetId(),
                                validation.getIdAgentValidateur(),
                                validation.getProjetId()
                        );
                    }
                    break;

                case "rejeterProjet":
                    if (result instanceof ValidationDTO) {
                        ValidationDTO validation = (ValidationDTO) result;
                        historiqueService.logAction(
                                "Rejet niveau " + validation.getNiveau() + " du projet ID: " + validation.getProjetId(),
                                validation.getIdAgentValidateur(),
                                validation.getProjetId()
                        );
                    }
                    break;
            }
        } catch (Exception e) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.error("❌ Erreur lors du logging dans la BD", e);
        }
    }
}
package com.smartek.certificationbadgeservice.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service for handling authorization checks.
 * Implements role-based and resource-based authorization logic.
 */
@Service
@Slf4j
public class AuthorizationService {
    
    /**
     * Get the currently authenticated user details
     */
    public UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
            return (UserDetailsImpl) authentication.getPrincipal();
        }
        throw new IllegalStateException("No authenticated user found");
    }
    
    /**
     * Get the current user's ID
     */
    public Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }
    
    /**
     * Get the current user's role
     */
    public String getCurrentUserRole() {
        return getCurrentUser().getRole();
    }
    
    /**
     * Check if the current user has a specific role
     */
    public boolean hasRole(String role) {
        try {
            String currentRole = getCurrentUserRole();
            return currentRole.equals(role);
        } catch (Exception e) {
            log.error("Error checking role: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if the current user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        try {
            String currentRole = getCurrentUserRole();
            for (String role : roles) {
                if (currentRole.equals(role)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Error checking roles: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if the current user can access learner data.
     * Rules:
     * - ADMIN can access any learner data
     * - TRAINER can access any learner data
     * - RH_COMPANY can access any learner data
     * - RH_SMARTEK can access any learner data
     * - LEARNER can only access their own data
     */
    public boolean canAccessLearnerData(Long learnerId) {
        try {
            UserDetailsImpl currentUser = getCurrentUser();
            String role = currentUser.getRole();
            Long userId = currentUser.getUserId();
            
            // ADMIN, TRAINER, and RH users can access any learner data
            if (role.equals("ADMIN") || role.equals("TRAINER") || 
                role.equals("RH_COMPANY") || role.equals("RH_SMARTEK")) {
                log.debug("User {} with role {} granted access to learner {}", userId, role, learnerId);
                return true;
            }
            
            // LEARNER can only access their own data
            if (role.equals("LEARNER")) {
                boolean canAccess = userId.equals(learnerId);
                if (!canAccess) {
                    log.warn("Learner {} attempted to access data for learner {}", userId, learnerId);
                }
                return canAccess;
            }
            
            // Unknown role - deny access
            log.warn("User {} with unknown role {} attempted to access learner data", userId, role);
            return false;
            
        } catch (Exception e) {
            log.error("Error checking learner data access: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if the current user is an admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }
    
    /**
     * Check if the current user is a trainer
     */
    public boolean isTrainer() {
        return hasRole("TRAINER");
    }
    
    /**
     * Check if the current user is a learner
     */
    public boolean isLearner() {
        return hasRole("LEARNER");
    }
    
    /**
     * Check if the current user is an RH user (RH_COMPANY or RH_SMARTEK)
     */
    public boolean isRHUser() {
        return hasAnyRole("RH_COMPANY", "RH_SMARTEK");
    }
    
    /**
     * Check if the current user can create or modify templates
     */
    public boolean canManageTemplates() {
        return hasAnyRole("ADMIN", "TRAINER");
    }
    
    /**
     * Check if the current user can award badges or certifications
     */
    public boolean canAwardAchievements() {
        return hasAnyRole("ADMIN", "TRAINER");
    }
    
    /**
     * Check if the current user can view statistics
     */
    public boolean canViewStatistics() {
        return hasAnyRole("ADMIN", "RH_COMPANY", "RH_SMARTEK");
    }
}

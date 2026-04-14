package com.smartek.certificationbadgeservice.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Custom UserDetails implementation to hold user information from JWT token.
 * Contains user ID, username (email), and role extracted from the JWT.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    
    private Long userId;
    private String username;
    private String role;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Map role from JWT to Spring Security authority
        // Role format: ROLE_<role_name>
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }
    
    @Override
    public String getPassword() {
        // No password needed as authentication is done via JWT
        return null;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return true;
    }
    
    /**
     * Get the user ID from the JWT token
     */
    public Long getUserId() {
        return userId;
    }
    
    /**
     * Get the role from the JWT token
     */
    public String getRole() {
        return role;
    }
}

package com.mediaserver.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public final class SecurityUtil {

    private SecurityUtil() {}

    public static boolean isRoot() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null &&
               auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ROOT"));
    }

    public static String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return null;
        if (auth.getPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) auth.getPrincipal()).getId();
        }
        if (auth.getPrincipal() instanceof RootUserPrincipal) {
            return ((RootUserPrincipal) auth.getPrincipal()).getId();
        }
        return null;
    }
}
package com.getech.energy.platformbasic.auth;

public final class AuthContext {

    private static final ThreadLocal<CurrentUser> CURRENT = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(CurrentUser user) {
        CURRENT.set(user);
    }

    public static CurrentUser requireUser() {
        CurrentUser user = CURRENT.get();
        if (user == null) {
            throw new IllegalStateException("Missing authenticated user");
        }
        return user;
    }

    public static void clear() {
        CURRENT.remove();
    }
}

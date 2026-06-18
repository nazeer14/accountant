package com.pack.controller;

import com.pack.dto.UserCreateRequest;
import com.pack.dto.UserResponse;
import com.pack.dto.UserUpdateRequest;
import com.pack.enums.Role;
import com.pack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "USER API", description = "User Money Management Operations")
public class UserController {

    private final UserService userService;

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // POST   /api/v1/users                  → Register / create a user
    // GET    /api/v1/users/{id}             → Fetch user by ID
    // GET    /api/v1/users/email/{email}    → Fetch user by email (team invite look-up)
    // GET    /api/v1/users/phone/{phone}    → Fetch user by phone
    // PUT    /api/v1/users/{id}             → Update profile (name, phone, currency, role)
    // DELETE /api/v1/users/{id}             → Remove account
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Register a new user.
     * Used during onboarding; salary/expense features become available after verification.
     */
    @PostMapping
    @Operation(
            summary = "Create User",
            description = "Creates a New User "
    )
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserResponse created = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * Get user profile by UUID.
     * Referenced from Dashboard, Savings Goals, and Team Expense screens.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    /**
     * Get user by email address.
     * Needed by Team Expense Sharing when inviting members via email.
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    /**
     * Get user by phone number.
     */
    @GetMapping("/phone/{phone}")
    public ResponseEntity<UserResponse> getUserByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(userService.getUserByPhone(phone));
    }

    /**
     * Update user profile.
     * Supports updating fullName, phone, currency (multi-currency income tracking),
     * and role (admin / member for Team Expense Sharing).
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }

    /**
     * Delete a user account and all associated data.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Listing & Search
    // GET /api/v1/users                     → All users (admin)
    // GET /api/v1/users/role/{role}         → Filter by role
    // GET /api/v1/users/search?name=        → Full-name search (team invite)
    // POST /api/v1/users/by-emails          → Bulk lookup for team invitations
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * List all users. Intended for admin dashboards / analytics.
     */
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * Filter users by role (ADMIN / MEMBER / USER).
     * Used in Team Expense Sharing to list team admins or members.
     */
    @GetMapping("/role/{role}")
    public ResponseEntity<List<UserResponse>> getUsersByRole(@PathVariable Role role) {
        return ResponseEntity.ok(userService.getUsersByRole(role));
    }

    /**
     * Search users by full name.
     * Used in the Team Expense Sharing invite flow.
     */
    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String name) {
        return ResponseEntity.ok(userService.searchUsersByName(name));
    }

    /**
     * Bulk user look-up by email list.
     * Used when creating a team and inviting multiple members at once.
     */
    @PostMapping("/by-emails")
    public ResponseEntity<List<UserResponse>> getUsersByEmails(@RequestBody List<String> emails) {
        return ResponseEntity.ok(userService.getUsersByEmails(emails));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Verification
    // PATCH /api/v1/users/{id}/verify       → Mark user as verified
    // PATCH /api/v1/users/{id}/unverify     → Revoke verification
    // GET   /api/v1/users/unverified        → List unverified accounts (admin)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verify a user account (e.g., after OTP / email confirmation).
     * Unlocks Expense Tracking, Salary & Income, and Savings Goal features.
     */
    @PatchMapping("/{id}/verify")
    public ResponseEntity<UserResponse> verifyUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.verifyUser(id));
    }

    /**
     * Revoke verification (admin action).
     */
    @PatchMapping("/{id}/unverify")
    public ResponseEntity<UserResponse> unverifyUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.unverifyUser(id));
    }

    /**
     * List all unverified users for admin review.
     */
    @GetMapping("/unverified")
    public ResponseEntity<List<UserResponse>> getUnverifiedUsers() {
        return ResponseEntity.ok(userService.getUnverifiedUsers());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Currency
    // PATCH /api/v1/users/{id}/currency     → Update preferred currency
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Update the user's preferred currency.
     * Affects Dashboard summary cards and net balance formula display.
     * Supports multi-currency income tracking (e.g., INR, USD, EUR).
     */
    @PatchMapping("/{id}/currency")
    public ResponseEntity<UserResponse> updateCurrency(
            @PathVariable UUID id,
            @RequestParam String currency) {
        return ResponseEntity.ok(userService.updateCurrency(id, currency));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Existence checks
    // GET /api/v1/users/exists/email/{email}
    // GET /api/v1/users/exists/phone/{phone}
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Check whether an email is already registered.
     * Used during sign-up and team invite validation.
     */
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Map<String, Boolean>> existsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(Map.of("exists", userService.existsByEmail(email)));
    }

    /**
     * Check whether a phone number is already registered.
     */
    @GetMapping("/exists/phone/{phone}")
    public ResponseEntity<Map<String, Boolean>> existsByPhone(@PathVariable String phone) {
        return ResponseEntity.ok(Map.of("exists", userService.existsByPhone(phone)));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Stats  (admin / analytics)
    // GET /api/v1/users/stats
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Aggregate user statistics for the admin analytics dashboard.
     * Returns total counts by verification status and role.
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        Map<String, Object> stats = Map.of(
                "totalUsers",       userService.getAllUsers().size(),
                "verifiedUsers",    userService.countVerifiedUsers(),
                "unverifiedUsers",  userService.countUnverifiedUsers(),
                "adminCount",       userService.countUsersByRole(Role.ADMIN),
                "memberCount",      userService.countUsersByRole(Role.MEMBER),
                "userCount",        userService.countUsersByRole(Role.USER)
        );
        return ResponseEntity.ok(stats);
    }
}
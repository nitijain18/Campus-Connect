package com.campusconnect.controller;

import com.campusconnect.dto.LoginRequest;
import com.campusconnect.dto.SignupRequest;
import com.campusconnect.dto.VerifyOtpRequest;
import com.campusconnect.entity.College;
import com.campusconnect.entity.OtpVerification;
import com.campusconnect.entity.RefreshToken;
import com.campusconnect.entity.User;
import com.campusconnect.enums.UserRole;
import com.campusconnect.exception.RateLimitExceededException;
import com.campusconnect.repository.CollegeRepository;
import com.campusconnect.repository.OtpVerificationRepository;
import com.campusconnect.repository.RefreshTokenRepository;
import com.campusconnect.repository.UserRepository;
import com.campusconnect.response.AuthResponse;
import com.campusconnect.security.JwtUtil;
import com.campusconnect.security.RateLimiterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
    private final CollegeRepository collegeRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JavaMailSender mailSender;
    private final RateLimiterService rateLimiterService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        if (!rateLimiterService.tryConsume("signup:" + request.getEmail(), 5, 3600)) {
            throw new RateLimitExceededException("Too many signup attempts. Please try again later.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already registered");
        }

        College college = null;
        if (request.getCollegeId() != null) {
            college = collegeRepository.findById(request.getCollegeId())
                    .orElseThrow(() -> new RuntimeException("College not found"));
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(UserRole.ROLE_STUDENT);
        user.setIsAdmin(false);
        user.setIsVerified(false);
        user.setCollege(college);

        userRepository.save(user);
        sendOtp(request.getEmail());

        return ResponseEntity.ok("Signup successful. OTP sent to email.");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        if (!rateLimiterService.tryConsume("verify-otp:" + request.getEmail(), 5, 900)) {
            throw new RateLimitExceededException("Too many verification attempts. Please try again in 15 minutes.");
        }

        List<OtpVerification> otps = otpVerificationRepository.findByEmailOrderByCreatedAtDesc(request.getEmail());

        if (otps.isEmpty()) {
            return ResponseEntity.badRequest().body("No OTP found for this email");
        }

        OtpVerification latest = otps.get(0);

        if (latest.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpVerificationRepository.deleteAll(otps);
            return ResponseEntity.badRequest().body("OTP expired. Please request a new one.");
        }

        if (!latest.getOtp().equals(request.getOtp())) {
            return ResponseEntity.badRequest().body("Invalid OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setIsVerified(true);
        userRepository.save(user);

        otpVerificationRepository.deleteAll(otps);

        return ResponseEntity.ok("Email verified successfully. You can now log in.");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        if (!rateLimiterService.tryConsume("login:" + request.getEmail(), 5, 900)) {
            throw new RateLimitExceededException("Too many login attempts. Please try again in 15 minutes.");
        }

        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(user.getIsVerified())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Please verify your email first");
        }

        // Successful login - reset the rate limit counter for this email
        rateLimiterService.reset("login:" + request.getEmail());

        String accessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getIsAdmin());
        String refreshTokenStr = createRefreshToken(user);

        AuthResponse response = new AuthResponse(
                accessToken,
                refreshTokenStr,
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getIsAdmin(),
                "Login successful"
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String requestRefreshToken = body.get("refreshToken");
        if (requestRefreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token is required");
        }

        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired. Please log in again.");
        }

        User user = refreshToken.getUser();
        String newAccessToken = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name(), user.getIsAdmin());

        return ResponseEntity.ok(Map.of("token", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody Map<String, String> body) {
        String requestRefreshToken = body.get("refreshToken");
        if (requestRefreshToken != null) {
            refreshTokenRepository.findByToken(requestRefreshToken).ifPresent(refreshTokenRepository::delete);
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        if (!rateLimiterService.tryConsume("resend-otp:" + email, 3, 600)) {
            throw new RateLimitExceededException("Too many OTP requests. Please try again in 10 minutes.");
        }

        if (!userRepository.existsByEmail(email)) {
            return ResponseEntity.badRequest().body("No account found with this email");
        }
        sendOtp(email);
        return ResponseEntity.ok("OTP resent to email");
    }

    private String createRefreshToken(User user) {
        refreshTokenRepository.deleteByUser(user); // one active refresh token per user

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setUser(user);
        refreshToken.setExpiryDate(LocalDateTime.now().plusDays(30));
        refreshTokenRepository.save(refreshToken);

        return refreshToken.getToken();
    }

    private void sendOtp(String email) {
        String otp = generateSixDigitOtp();

        OtpVerification otpEntity = new OtpVerification();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpVerificationRepository.save(otpEntity);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("CampusConnect - Email Verification OTP");
        message.setText("Your OTP for CampusConnect verification is: " + otp + "\nThis code expires in 10 minutes.");
        mailSender.send(message);
    }

    private String generateSixDigitOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
package com.sujalsamai.auth_service.controller;

import com.sujalsamai.auth_service.dto.LoginRequestDTO;
import com.sujalsamai.auth_service.dto.LoginResponseDTO;
import com.sujalsamai.auth_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
public class AuthController
{

   private static final Logger log = LoggerFactory.getLogger(AuthController.class);
   private final AuthService authService;

   public AuthController(AuthService authService)
   {
      this.authService = authService;
   }

   @Operation(summary = "Generate token on user login")
   @PostMapping("/login")
   public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO requestDTO)
   {
      Optional<String> tokenOptional = authService.authenticate(requestDTO);

      if (tokenOptional.isEmpty())
      {
         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      String token = tokenOptional.get();
      log.info("Token generated successfully for user email: {}", requestDTO.getEmail());
      return ResponseEntity.ok(new LoginResponseDTO(token));
   }


   @Operation(summary = "Validate Token")
   @GetMapping("/validate")
   public ResponseEntity<Void> validateToken(@RequestHeader("Authorization") String authHeader)
   {
      if (authHeader == null || !authHeader.startsWith("Bearer"))
      {
         return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      return authService.validateToken(authHeader.substring(7))
               ? ResponseEntity.ok().build()
               : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
   }
}

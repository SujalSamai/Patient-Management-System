package com.sujalsamai.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequestDTO
{
   @NotBlank(message = "email is required")
   @Email(message = "Email should be a valid email address")
   private String email;

   @NotBlank(message = "Password must not be blank")
   @Size(min = 8, message = "Password must atleast be 8 characters long")
   private String password;

   public String getEmail()
   {
      return email;
   }

   public void setEmail(String email)
   {
      this.email = email;
   }

   public String getPassword()
   {
      return password;
   }

   public void setPassword(String password)
   {
      this.password = password;
   }
}

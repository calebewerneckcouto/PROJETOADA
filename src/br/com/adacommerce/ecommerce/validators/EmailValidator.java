package br.com.adacommerce.ecommerce.validators;

import java.util.regex.Pattern;

import br.com.adacommerce.ecommerce.exceptions.ValidationException;

public class EmailValidator {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE
    );
    
    public static void validar(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("Email não pode estar vazio");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Email inválido: " + email);
        }
    }
}
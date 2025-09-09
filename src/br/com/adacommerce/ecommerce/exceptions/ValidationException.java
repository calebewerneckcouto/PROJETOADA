package br.com.adacommerce.ecommerce.exceptions;

public class ValidationException extends BusinessException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ValidationException(String message) {
        super(message);
    }
}
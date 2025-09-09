package br.com.adacommerce.ecommerce.exceptions;

public class InsufficientStockException extends BusinessException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InsufficientStockException(String message) {
        super(message);
    }
}
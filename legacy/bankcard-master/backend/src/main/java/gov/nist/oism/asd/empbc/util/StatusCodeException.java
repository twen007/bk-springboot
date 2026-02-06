/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gov.nist.oism.asd.empbc.util;

/**
 *
 * @author xinweiw
 */
public class StatusCodeException extends Exception{
    private static final long serialVersionUID = 1L;
	private int errorCode;

    public StatusCodeException(StatusCode statusCode) {
        super(statusCode.getDescription());
        this.errorCode = statusCode.getCode();
    }

    public StatusCodeException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;

    }

    /**
     * @return the errorCode
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * @param errorCode the errorCode to set
     */
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}

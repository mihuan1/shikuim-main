package cn.xyz.commons.ex;



public class BizException extends Exception {
    private static final long serialVersionUID = 133223423413241L;

    private int errorCode;
    private String errorMessage;

    public BizException() {
        super();
    }

    public BizException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public BizException(Throwable e) {
        super(e);
    }

    public BizException(String errorMessage, Throwable e) {
        super(errorMessage,e);
        this.errorCode = 0;
        this.errorMessage = errorMessage;
        this.addSuppressed(e);
    }

    public BizException(String errorMessage) {
        super(errorMessage);
        this.errorCode = 0;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

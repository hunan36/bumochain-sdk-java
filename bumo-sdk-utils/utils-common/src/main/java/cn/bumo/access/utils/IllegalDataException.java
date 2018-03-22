package cn.bumo.access.utils;

/**
 * 无效数据异常；
 *
 * @author 布萌
 */
public class IllegalDataException extends RuntimeException{

    private static final long serialVersionUID = 5834019788898871654L;

    public IllegalDataException(){
    }

    public IllegalDataException(String message){
        super(message);
    }

    public IllegalDataException(String message, Throwable cause){
        super(message, cause);
    }
}

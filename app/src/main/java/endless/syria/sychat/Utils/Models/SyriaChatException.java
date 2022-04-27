package endless.syria.sychat.Utils.Models;

public class SyriaChatException extends Exception {

    public SyriaChatException(String error) {
        super(error);
    }
    public SyriaChatException(String error,Throwable err){
        super(error,err);
    }
}

package joao.chat.commonPackage;


import java.io.Serializable;

/*
 * Representation of the message
 */
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;
    private String username;
    private long timeStamp;

    public Message(String message, String username) {
        super();
        this.message = message;
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }
    public String toString() {
        return this.message; 
    }
    
}

package joao.chat.commonPackage;

import java.io.Serializable;
import java.security.PublicKey;

/*
 * A record of a lookup table
 */
public class Record implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;			//An identificator, must be unique
    private long timeStamp;			//A timestamp used to store the last time the session key of this record was updated (useful for periodic key updates)

    public Record(String username, String password, long timeStamp, PublicKey pubKey) {
        super();
        this.username = username;
        this.timeStamp = timeStamp;
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
}

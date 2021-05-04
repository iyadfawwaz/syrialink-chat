
package endless.syria.sychat.Utils.Models;


public class MessageText {

        public String message;
        public String user;
        private String time;
        private int type;

        private MessageText(){

        }

        public MessageText(String user, String message,String time,int type) {
            this.user = user;
            this.message = message;
            this.time = time;
            this.type = type;
        }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUser() {
            return user;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }


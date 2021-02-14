package org.codebrainz.picearn;

public class MessagesModel {

    private String image_bg;
    private String username;
    private int amount;
    private String user_id;
    private String message_id;
    private String earning_address;
    private long timestamp;

    public MessagesModel(){

    }

    public MessagesModel(String image_bg, String username, int amount, String user_id, String message_id, String earning_address, long timestamp) {
        this.image_bg = image_bg;
        this.username = username;
        this.amount = amount;
        this.user_id = user_id;
        this.message_id = message_id;
        this.earning_address = earning_address;
        this.timestamp = timestamp;
    }

    public String getImage_bg() {
        return image_bg;
    }

    public void setImage_bg(String image_bg) {
        this.image_bg = image_bg;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public String getEarning_address() {
        return earning_address;
    }

    public void setEarning_address(String earning_address) {
        this.earning_address = earning_address;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}

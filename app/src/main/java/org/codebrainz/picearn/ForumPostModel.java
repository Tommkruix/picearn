package org.codebrainz.picearn;

public class ForumPostModel {

    private String image_bg;
    private String username;
    private String likes;
    private String body;
    private long timestamp;
    private String image;
    private String user_id_post_id;
    private String post_id;
    private String user_id;

    public ForumPostModel(){

    }

    public ForumPostModel(String image_bg, String username, String likes, String body, long timestamp, String image, String user_id_post_id, String post_id, String user_id) {
        this.image_bg = image_bg;
        this.username = username;
        this.likes = likes;
        this.body = body;
        this.timestamp = timestamp;
        this.image = image;
        this.user_id_post_id = user_id_post_id;
        this.post_id = post_id;
        this.user_id = user_id;
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

    public String getLikes() {
        return likes;
    }

    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUser_id_post_id() {
        return user_id_post_id;
    }

    public void setUser_id_post_id(String user_id_post_id) {
        this.user_id_post_id = user_id_post_id;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}

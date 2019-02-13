package com.naimsplanet.photoblog;


import java.util.Date;

public class Blog extends BlogPostId {
    private String image_url;
    private String desc;
    private String user_id;
    private String thumb_image;
    private Date timestamp;


    public Blog() {

    }

    public Blog(String image_url, String desc, String user_id, String thumb_image, Date timestamp) {
        this.image_url = image_url;
        this.desc = desc;
        this.user_id = user_id;
        this.thumb_image = thumb_image;
        this.timestamp = timestamp;

    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }


}

/*
 * Company : AndroPlaza
 * Detailed : Software Development Company in Sri Lanka
 * Developer : Buddhika
 * Contact : support@androplaza.shop
 * Whatsapp : +94711920144
 */

package com.whocaller.spamdetector.modal;

import com.google.gson.annotations.SerializedName;

public class UserProfile {
    @SerializedName("first_name")
    private String first_name;

    @SerializedName("last_name")
    private String last_name;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;


    @SerializedName("imgUrl")
    private String imgUrl;

    public UserProfile(String firstName, String lastName, String email, String phone,String password,String imgUrl) {
        this.first_name = firstName;
        this.last_name = lastName;
        this.email = email;
        this.phone = phone;
        this.imgUrl = imgUrl;
    }


    public String getImgUrl() {
        return imgUrl;
    }


    public String getFirstName() {
        return first_name;
    }


    public String getLastName() {
        return last_name;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public static class UserProfileResponse {
        @SerializedName("status")
        private String status;
        @SerializedName("data")
        private UserProfile data;
        @SerializedName("message")
        private String message;
        public String getStatus() {
            return status;
        }

        public UserProfile getData() {
            return data;
        }

        public void setData(UserProfile data) {
            this.data = data;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}

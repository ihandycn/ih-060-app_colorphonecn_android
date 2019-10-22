package com.honeycomb.colorphone.http.bean;

public class UserBean {

    /**
     * user_info : {"created":1571645177000,"gender":"man","signature":"hahaha","name":"test_user","user_id":"abcdefg","updated":1571645177000,"head_image_url":"https://dev-fastgear.s3.amazonaws.com/storage/7CHcZNdbV7ZcVNbd.test.png","session_token":null,"birthday":"2019-08-02"}
     * token : null
     */

    private UserInfoBean user_info;
    private String token;

    public UserInfoBean getUser_info() {
        return user_info;
    }

    public void setUser_info(UserInfoBean user_info) {
        this.user_info = user_info;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public static class UserInfoBean {
        /**
         * created : 1571645177000
         * gender : man
         * signature : hahaha
         * name : test_user
         * user_id : abcdefg
         * updated : 1571645177000
         * head_image_url : https://dev-fastgear.s3.amazonaws.com/storage/7CHcZNdbV7ZcVNbd.test.png
         * session_token : null
         * birthday : 2019-08-02
         */

        private long created;
        private String gender;
        private String signature;
        private String name;
        private String user_id;
        private long updated;
        private String head_image_url;
        private Object session_token;
        private String birthday;

        public long getCreated() {
            return created;
        }

        public void setCreated(long created) {
            this.created = created;
        }

        public String getGender() {
            return gender;
        }

        public void setGender(String gender) {
            this.gender = gender;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public long getUpdated() {
            return updated;
        }

        public void setUpdated(long updated) {
            this.updated = updated;
        }

        public String getHead_image_url() {
            return head_image_url;
        }

        public void setHead_image_url(String head_image_url) {
            this.head_image_url = head_image_url;
        }

        public Object getSession_token() {
            return session_token;
        }

        public void setSession_token(Object session_token) {
            this.session_token = session_token;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }
    }
}

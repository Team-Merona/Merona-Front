package com.example.merona.user;

//사용자 계정 정보 모델 클래스

public class UserAccount {
    private String idToken;  //firebase Uid(고유 토큰 정보)
    private String emailId;  //이메일 아이디
    private String password;  //비밀번호

    public UserAccount() { }

    public String getEmailId() {return emailId;}

    public void setEmailId(String emailId) {this.emailId = emailId;}

    public String getPassword() {return password;}

    public void setPassword(String password) {this.password = password;}

    public String getIdToken() {return idToken;}

    public void setIdToken(String idToken) {this.idToken = idToken;}
}

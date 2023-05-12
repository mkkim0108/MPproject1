package com.example.mpproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class UserAccount{

    public UserAccount(){

    }


    public String getIdToken() {
        return idToken;
    }

    public void  setIdToken(String idToken){

        this.idToken = idToken;
    }

    private  String idToken;

    public String getEmailId() {

        return emailId;
    }

    public void  setIEmailId(String emailId){

        this.emailId = emailId;
    }

    private  String emailId;



    public String getPassword() {

        return password;
    }

    public void  setPassword(String password){

        this.password = password;
    }

    private  String password;


}
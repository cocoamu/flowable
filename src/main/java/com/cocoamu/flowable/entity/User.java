package com.cocoamu.flowable.entity;

public class User implements org.flowable.idm.api.User {


    private static final long serialVersionUID = -6247124918475041929L;
    private String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getFirstName() {
        return null;
    }

    @Override
    public void setFirstName(String s) {

    }

    @Override
    public void setLastName(String s) {

    }

    @Override
    public String getLastName() {
        return null;
    }

    @Override
    public void setDisplayName(String s) {

    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public void setEmail(String s) {

    }

    @Override
    public String getEmail() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public void setPassword(String s) {

    }

    @Override
    public String getTenantId() {
        return null;
    }

    @Override
    public void setTenantId(String s) {

    }

    @Override
    public boolean isPictureSet() {
        return false;
    }
}

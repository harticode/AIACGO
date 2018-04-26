package me.aitbaali.aiacgo;

public class User {

    private String nameuser;
    private String PhoneNumber;
    private String profilePic;


    public User(){}

    public User(String nameuser, String phoneNumber, String profilePic) {
        this.nameuser = nameuser;
        PhoneNumber = phoneNumber;
        this.profilePic = profilePic;
    }


    public String getNameuser() {
        return nameuser;
    }

    public void setNameuser(String nameuser) {
        this.nameuser = nameuser;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getProfilePic() {
        return profilePic;
    }

    public void setProfilePic(String profilePic) {
        this.profilePic = profilePic;
    }

}

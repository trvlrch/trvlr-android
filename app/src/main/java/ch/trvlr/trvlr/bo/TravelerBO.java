package ch.trvlr.trvlr.bo;

public class TravelerBO {


    // ----- State.

    private int id;
    private String firstname;
    private String lastname;
    private String email;
    private String uid;


    // ----- Constructors.

    public TravelerBO(int id, String firstname, String lastname, String email, String uid) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.uid = uid;
    }


    // ----- BO methods.

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }


    // ----- Helper methods.

    public String getFullname() {
        return this.getFirstname() + " " + this.getLastname();
    }
}

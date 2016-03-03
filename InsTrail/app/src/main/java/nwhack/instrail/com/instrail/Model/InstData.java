package nwhack.instrail.com.instrail.Model;

/**
 * Created by Rob on 2/27/2016.
 */
public class InstData {

    private String userPhoto;
    private String username;
    private String imageLocation;

    private String smallURL;
    private String mediumURL;
    private String largeURL;
    private boolean isStub = true;

    public InstData(String small, String medium, String large, String userphoto, String username, String location) {
        this.smallURL = small;
        this.mediumURL = medium;
        this.largeURL = large;
        this.username = username;
        this.userPhoto = userphoto;
        this.imageLocation = location;
        this.isStub = true;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getUsername() {
        if (username == null) {
            username = "username";
        }
        return "@"+username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getImageLocation() {
        if (imageLocation == null) {
            imageLocation = "Unknown";
        }
        return imageLocation;
    }

    public void setImageLocation(String imageLocation) {
        this.imageLocation = imageLocation;
    }

    public boolean isStub() {
        return isStub;
    }

    public void setIsStub(boolean b) {
        this.isStub = b;
    }

    public boolean getIsStub() {
        return this.isStub;
    }


    public String getSmallURL() {
        return smallURL;
    }

    public void setSmallURL(String smallURL) {
        this.smallURL = smallURL;
    }

    public String getMediumURL() {
        return mediumURL;
    }

    public void setMediumURL(String mediumURL) {
        this.mediumURL = mediumURL;
    }

    public String getLargeURL() {
        return largeURL;
    }

    public void setLargeURL(String largeURL) {
        this.largeURL = largeURL;
    }
}

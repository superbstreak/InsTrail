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

    public InstData(String small, String medium, String large, String userPhoto, String username, String location) {
        this.smallURL = small;
        this.mediumURL = medium;
        this.largeURL = large;
        this.username = username;
        this.userPhoto = userPhoto;
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
        return "@" + username;
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

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstData instData = (InstData) o;

        if (isStub != instData.isStub) return false;
        if (!smallURL.equals(instData.smallURL)) return false;
        if (!mediumURL.equals(instData.mediumURL)) return false;
        return largeURL.equals(instData.largeURL);

    }

    @Override
    public int hashCode() {
        int result = smallURL.hashCode();
        result = 31 * result + mediumURL.hashCode();
        result = 31 * result + largeURL.hashCode();
        result = 31 * result + (isStub ? 1 : 0);
        return result;
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

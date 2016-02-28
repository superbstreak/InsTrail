package nwhack.instrail.com.instrail.Model;

/**
 * Created by Rob on 2/27/2016.
 */
public class InstData {

    private String smallURL;
    private String mediumURL;
    private String largeURL;
    private boolean isStub = true;

    public InstData(String small, String medium, String large) {
        this.smallURL = small;
        this.mediumURL = medium;
        this.largeURL = large;
        this.isStub = true;
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

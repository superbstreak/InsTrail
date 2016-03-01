package nwhack.instrail.com.instrail;

/**
 * Created by Rob on 2/27/2016.
 */
public class Constant {

    public static final int ZOOM_LEVEL = 9;
    public static final int MAX_CALL = 50;
    public static final double VANCOUVER_LAT = 49.485079;
    public static final double VANCOUVER_LON = -122.985231;

    public static final String PHOTO_INTENT_TAG = "PHOTO_ORGIN";
    public static final String PHOTO_TAG_MAIN = "ORG_MAIN_ACTIVITY";
    public static final String PHOTO_TAG_TRAIL = "ORG_TRAIL_ACTIVITY";
    public static final String TRAIL_POSITION_TAG = "TRAIL_POS_TAG";

    public static final String CLIENT_ID = "d91dcfac9ed346478e76999806a15b59";
    public static final String CLIENT_SECRET = "cc8e2069c8c64e29900060d94475b71d";
    public static final String REDIRECT_URI = "com-instrail://instagramredirect";
    public static final String ZAMA_ZINGO_ACCESS_TOKEN = "2257996576.cf0499d.08834443f30a4d278c28fcaf41af2f71";
    public static final String ZAMA_ZINGO_USER_ID = "2257996576";
    public static final String TAG = "vancouvertrails";

    public static final int REQUEST_IMAGE_CAPTURE = 1;

    public static final String FIRST_URL = "https://api.instagram.com/v1/tags/" + TAG + "/media/recent?access_token=" + ZAMA_ZINGO_ACCESS_TOKEN;

}

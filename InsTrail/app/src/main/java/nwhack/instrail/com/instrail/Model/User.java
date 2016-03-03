package nwhack.instrail.com.instrail.Model;

import net.londatiga.android.instagram.InstagramSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Borislav on 3/3/2016.
 */
public class User {

    private String accessToken;
    private String userId;
    private Map<String, Trail> trailMapper;
    private List<Trail> trails;


    public User(InstagramSession session) {
        this.accessToken = session.getAccessToken();
        this.userId = session.getUser().id;
        trailMapper = new HashMap<>();
        trails = new ArrayList<>();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Map<String, Trail> getTrailMapper() {
        return trailMapper;
    }

    public void setTrailMapper(Map<String, Trail> trailMapper) {
        this.trailMapper = trailMapper;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<Trail> getTrails() {
        return trails;
    }

    public void setTrails(List<Trail> trails) {
        this.trails = trails;
    }
}

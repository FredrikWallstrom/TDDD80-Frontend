package com.example.frewa814.livekrubb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Fredrik on 2015-04-13.
 */
public class ActivatedPerson{

    private static final String PROFILE_PICTURE_TAG = "profile_picture";
    private static final String USERNAME_TAG = "username";
    public static String activatedProfilePicture;
    public static String activatedUsername;

    public ActivatedPerson(JSONObject jsonObject) {
        try {
            activatedProfilePicture = jsonObject.getString(PROFILE_PICTURE_TAG);
            activatedUsername = jsonObject.getString(USERNAME_TAG);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

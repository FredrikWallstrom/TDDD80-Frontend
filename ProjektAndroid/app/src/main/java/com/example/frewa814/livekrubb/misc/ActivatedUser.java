package com.example.frewa814.livekrubb.misc;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Save all information about the user if the user succeed to log in.
 * The fields are static so I can easy get them and compare where i need to do it.
 */
public class ActivatedUser {

    private static final String PROFILE_PICTURE_TAG = "profile_picture";
    private static final String USERNAME_TAG = "username";
    public static String activatedProfilePicture;
    public static String activatedUsername;

    public ActivatedUser(JSONObject jsonObject) {
        try {
            activatedProfilePicture = jsonObject.getString(PROFILE_PICTURE_TAG);
            activatedUsername = jsonObject.getString(USERNAME_TAG);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
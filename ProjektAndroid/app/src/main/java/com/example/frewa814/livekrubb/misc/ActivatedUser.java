package com.example.frewa814.livekrubb.misc;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Save all information about the user if the user succeed to log in.
 * The fields are static so I can easy get them and compare where i need to do it.
 */
public class ActivatedUser {


    /**
     * Constant tags for http requests.
     */
    private static final String PROFILE_PICTURE_TAG = "profile_picture";
    private static final String USERNAME_TAG = "username";
    private static final String ID_TAG = "id";

    /**
     * Fields that are static so we can get this information in every fragment/activity
     * there we need them.
     */
    public static String activatedProfilePicture;
    public static String activatedUsername;
    public static String activatedUserID;

    /**
     * Constructor for this class, will be running when one user succeed to log in.
     * Will take one JSONObject with the user as input.
     */
    public ActivatedUser(JSONObject jsonObject) {
        try {
            activatedUserID = jsonObject.getString(ID_TAG);
            activatedProfilePicture = jsonObject.getString(PROFILE_PICTURE_TAG);
            activatedUsername = jsonObject.getString(USERNAME_TAG);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

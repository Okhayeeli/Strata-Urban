package com.strataurban.strata.Security.oauth2;


import com.strataurban.strata.Enums.Provider;
import com.strataurban.strata.Exceptions.OAuth2AuthenticationProcessingException;

import java.util.Map;

// OAuth2UserInfoFactory.java
public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if(registrationId.equalsIgnoreCase(Provider.GOOGLE.toString())) {
            return new GoogleOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(Provider.FACEBOOK.toString())) {
            return new FacebookOAuth2UserInfo(attributes);
        } else if (registrationId.equalsIgnoreCase(Provider.TWITTER.toString())) {
            return new TwitterOAuth2UserInfo(attributes);
        } else {
            throw new OAuth2AuthenticationProcessingException("Sorry! Login with " + registrationId + " is not supported yet.");
        }
    }
}
package io.soffa.foundation.core.models;

import lombok.Data;

@Data
public class UserInfo {
    private String nickname;
    private String givenName;
    private String familyName;
    private String gender;
    private String city;
    private String country;
    private String email;
    private String phoneNumber;
}

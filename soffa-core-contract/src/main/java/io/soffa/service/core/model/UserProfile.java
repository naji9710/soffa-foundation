package io.soffa.service.core.model;

import lombok.Data;

@Data
public class UserProfile {
    private String nickname;
    private String givenName;
    private String familyName;
    private String gender;
    private String city;
    private String country;
    private String email;
    private String phoneNumber;
}

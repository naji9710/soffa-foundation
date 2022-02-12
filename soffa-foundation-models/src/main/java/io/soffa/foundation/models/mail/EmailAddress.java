package io.soffa.foundation.models.mail;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.soffa.foundation.annotations.JsonModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.validator.routines.EmailValidator;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonModel
public class EmailAddress  {

    private String name;
    private String address;

    public EmailAddress(String value) {
        if (value.contains("<")) {
            String[] parts = value.trim().split("<");
            name = parts[0].trim().replaceAll("^\"|\"$", "").trim();
            address = parts[1].split(">")[0];
        } else {
            address = value.trim();
        }
    }

    public static List<EmailAddress> of(String address) {
        return Collections.singletonList(new EmailAddress(address));
    }

    public static List<EmailAddress> of(List<String> addresses) {
        return addresses.stream().map(EmailAddress::new).collect(Collectors.toList());
    }

    @JsonIgnore
    public boolean isValid() {
        return EmailValidator.getInstance().isValid(address);
    }

    public static boolean isValid(EmailAddress address) {
        return address != null && address.isValid();
    }

    public static boolean isValid(List<EmailAddress> addresses) {
        if (addresses == null) {
            return false;
        }
        for (EmailAddress address : addresses) {
            if (!address.isValid()) {
                return false;
            }
        }
        return true;
    }

}

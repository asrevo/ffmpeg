package org.revo.Domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;
import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

/**
 * Created by ashraf on 17/04/17.
 */
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseUser {
    private String id;
    private String name;
    private String imageUrl;
    private String phone;
    private String info;
    private String email;
    @JsonProperty(access = WRITE_ONLY)
    private String password;
    @JsonProperty(access = WRITE_ONLY)
    private String currentPassword;
    private Date createdDate = new Date();

    @JsonProperty(access = WRITE_ONLY)
    private MultipartFile image;


    @Override
    @JsonProperty(access = READ_ONLY)
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + getId() + '\'' +
                " ,createdDate='" + getCreatedDate() + '\'' +
                " ,name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", phone='" + phone + '\'' +
                ", info='" + info + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
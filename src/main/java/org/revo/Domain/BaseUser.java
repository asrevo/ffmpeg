package org.revo.Domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

/**
 * Created by ashraf on 17/04/17.
 */
@Getter
@Setter
public abstract class BaseUser implements UserDetails {
    @JsonProperty(access = READ_ONLY)
    private boolean locked = true;
    @JsonProperty(access = READ_ONLY)
    private boolean enable = false;
    private Collection<GrantedAuthority> authorities;

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public void setAuthorities(Collection<GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enable;
    }
}
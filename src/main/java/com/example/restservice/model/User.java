package com.example.restservice.model;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserTheme userTheme = UserTheme.AUTO;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<CourseUser> courseUsers;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Submission> submissions;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private Set<Assignment> assignments;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OauthUser> oauthUsers;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="user")
    private Set<UserLog> userLogs;

    @OneToMany(cascade=CascadeType.ALL, mappedBy="user")
    private Set<Schedule> schedules;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    public void updateWithoutId(User userDetails) {
        if (userDetails.getEmail() != null) {
            this.email = userDetails.getEmail();
        }

        if (userDetails.getUsername() != null) {
            this.username = userDetails.getUsername();
        }

        if (userDetails.getPassword() != null) {
            this.password = userDetails.getPassword();
        }

        if (userDetails.getRole() != null) {
            this.role = userDetails.getRole();
        }

        if (userDetails.getUserTheme() != null) {
            this.userTheme = userDetails.getUserTheme();
        }
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password){
        this.password=password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username){
        this.username=username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email){
        this.email=email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id){
        this.id=id;
    }

    public Role getRole(){
        return role;
    }

    public void setRole(Role role){
        this.role=role;
    }

    public UserTheme getUserTheme(){
        return userTheme;
    }

    public void setUserTheme(UserTheme userTheme){
        this.userTheme=userTheme;
    }
}

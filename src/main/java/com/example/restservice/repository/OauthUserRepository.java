package com.example.restservice.repository;

import com.example.restservice.model.OauthUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OauthUserRepository extends JpaRepository<OauthUser, Long> {
    Optional<OauthUser> findByProviderAndSubject(String provider, String subject);
}

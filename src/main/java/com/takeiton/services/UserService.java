package com.takeiton.services;

import com.takeiton.models.AppUser;
import com.takeiton.repositories.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    AppUserRepository appUserRepository;

    public AppUser getAppUserForName(String ownerName) {
        Optional<AppUser> optionalAppUser = appUserRepository.findById(ownerName);
        if (optionalAppUser.isEmpty()) {
            return null;
        }
        return optionalAppUser.get();
    }
}

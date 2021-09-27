package com.takeiton.services;

import com.takeiton.models.Token;
import com.takeiton.repositories.TokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TokenService {

    @Autowired
    private TokenRepository tokenRepository;

    public void save(String username, String usertoken) {
        Token token = new Token(username, usertoken);
        tokenRepository.save(token);
    }

    public void delete(String username) {
        tokenRepository.deleteById(username);
    }

    public boolean isActiveToken(String username, String tokenString) {
        Optional<Token> token = tokenRepository.findById(username);
        return token.filter(value -> tokenString.equals(value.getToken())).isPresent();
    }

}

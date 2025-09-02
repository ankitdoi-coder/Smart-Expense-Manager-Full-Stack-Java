package com.ankit.Expence.Tracker.App.Service;

import org.springframework.stereotype.Service;

import com.ankit.Expence.Tracker.App.Model.User;

@Service
public interface UserService {
    void saveNewUser(User user);
    User findByEmail(String email);
    
}


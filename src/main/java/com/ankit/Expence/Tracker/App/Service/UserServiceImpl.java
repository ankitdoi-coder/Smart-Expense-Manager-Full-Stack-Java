package com.ankit.Expence.Tracker.App.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.ankit.Expence.Tracker.App.Model.User;
import com.ankit.Expence.Tracker.App.Repository.UserRepository;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void saveNewUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword())); // hash password
        repo.save(user);
    }

    @Override
    public User findByEmail(String email) {
        return repo.findByEmail(email);
                
}
}

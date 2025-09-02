package com.ankit.Expence.Tracker.App.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ankit.Expence.Tracker.App.Model.User;

public interface UserRepository extends JpaRepository<User,Long>{
   
    User findByEmail(String email);
    User findByName(String name);
}

package com.poss.clash.bot.daos;

import com.poss.clash.bot.daos.models.User;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@EnableScan
public interface UserDao extends CrudRepository<User, String> {

    CompletableFuture<User> findUserById(String id);

}

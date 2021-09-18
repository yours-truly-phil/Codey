package io.horrorshow.codey.data.repository;

import io.horrorshow.codey.data.entity.ElevatedUser;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;


public interface ElevatedUserRepository extends CrudRepository<ElevatedUser, String> {
    @NotNull Set<ElevatedUser> findAll();
}

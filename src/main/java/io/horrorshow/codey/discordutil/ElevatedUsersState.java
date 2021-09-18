package io.horrorshow.codey.discordutil;

import io.horrorshow.codey.data.entity.ElevatedUser;
import io.horrorshow.codey.data.repository.ElevatedUserRepository;
import javassist.NotFoundException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Slf4j
public class ElevatedUsersState {

    @Getter
    private final Map<String, ElevatedUser> elevatedUsers;
    private final ElevatedUserRepository elevatedUserRepository;


    public ElevatedUsersState(ElevatedUserRepository elevatedUserRepository) {
        this.elevatedUserRepository = elevatedUserRepository;
        this.elevatedUsers = elevatedUserRepository.findAll()
                .stream().collect(Collectors.toConcurrentMap(ElevatedUser::getUserId, elevatedUser -> elevatedUser));
        log.debug("loaded {} elevated users", elevatedUsers.size());
    }


    public CompletableFuture<ElevatedUser> addElevatedUser(String userId, String username) {
        if (!elevatedUsers.containsKey(userId)) {
            var elevatedUser = new ElevatedUser(userId, username);
            var savedElevatedUser = elevatedUserRepository.save(elevatedUser);
            elevatedUsers.put(savedElevatedUser.getUserId(), savedElevatedUser);
            return CompletableFuture.completedFuture(savedElevatedUser);
        } else {
            return CompletableFuture.failedFuture(new NotFoundException("User " + userId + " already with elevated privileges"));
        }
    }


    public CompletableFuture<ElevatedUser> removeElevatedUser(String userId) {
        if (elevatedUsers.containsKey(userId)) {
            var elevatedUser = elevatedUsers.remove(userId);
            elevatedUserRepository.delete(elevatedUser);
            return CompletableFuture.completedFuture(elevatedUser);
        } else {
            return CompletableFuture.failedFuture(new NotFoundException("User " + userId + " not found in elevated users"));
        }
    }
}

package io.horrorshow.codey.data.repository;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class Repositories {

    @Getter
    private final TimerRepository timerRepository;
    @Getter
    private final GithubChannelRepository githubChannelRepository;
    @Getter
    private final ElevatedUserRepository elevatedUserRepository;


    @Autowired
    public Repositories(TimerRepository timerRepository,
            GithubChannelRepository githubChannelRepository,
            ElevatedUserRepository elevatedUserRepository) {

        this.timerRepository = timerRepository;
        this.githubChannelRepository = githubChannelRepository;
        this.elevatedUserRepository = elevatedUserRepository;
    }

}

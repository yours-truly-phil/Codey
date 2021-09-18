package io.horrorshow.codey.data;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class Repositories {

    @Getter
    private final TimerRepository timerRepository;
    @Getter
    private final GithubChannelRepository githubChannelRepository;


    @Autowired
    public Repositories(TimerRepository timerRepository, GithubChannelRepository githubChannelRepository) {
        this.timerRepository = timerRepository;
        this.githubChannelRepository = githubChannelRepository;
    }

}

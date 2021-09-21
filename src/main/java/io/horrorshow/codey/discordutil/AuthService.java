package io.horrorshow.codey.discordutil;

import net.dv8tion.jda.api.entities.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final CodeyConfig codeyConfig;
    private final ApplicationState applicationState;


    @Autowired
    public AuthService(CodeyConfig codeyConfig, ApplicationState applicationState) {
        this.codeyConfig = codeyConfig;
        this.applicationState = applicationState;
    }


    public boolean isElevatedMember(Member member) {
        return member != null
               && ((codeyConfig.getOwnerId() != null && codeyConfig.getOwnerId().equals(member.getId()))
                   || applicationState.getElevatedUsersState().getElevatedUsers().containsKey(member.getId()));
    }


    public boolean hasCodeyRole(Member member) {
        return member != null
               && member.getRoles().stream()
                       .anyMatch(role -> codeyConfig.getRoles().contains(role.getName()));
    }
}

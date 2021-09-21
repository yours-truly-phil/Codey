package io.horrorshow.codey.discordutil;

import io.horrorshow.codey.data.entity.ElevatedUser;
import io.horrorshow.codey.data.repository.ElevatedUserRepository;
import io.horrorshow.codey.data.repository.GithubChannelRepository;
import io.horrorshow.codey.data.repository.Repositories;
import io.horrorshow.codey.data.repository.TimerRepository;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;


public class AuthServiceTest {

    CodeyConfig codeyConfig;
    AuthService authService;
    Repositories repositories;
    ApplicationState applicationState;
    @Mock JDA jda;
    @Mock TimerRepository timerRepository;
    @Mock GithubChannelRepository githubChannelRepository;
    @Mock ElevatedUserRepository elevatedUserRepository;


    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);

        var elevatedUsers = Set.of(
                new ElevatedUser("userId", "user name"),
                new ElevatedUser("otherUserId", "other user name")
        );
        when(elevatedUserRepository.findAll()).thenReturn(elevatedUsers);

        codeyConfig = new CodeyConfig();
        repositories = new Repositories(timerRepository, githubChannelRepository, elevatedUserRepository);
        applicationState = new ApplicationState(jda, repositories);
        authService = new AuthService(codeyConfig, applicationState);
    }


    @Test
    void hasCodeyRole() {
        codeyConfig.getRoles().add("CodeyRole");
        var member = Mockito.mock(Member.class);

        when(member.getRoles()).thenReturn(List.of());
        assertThat(authService.hasCodeyRole(member)).isFalse();

        var role = Mockito.mock(Role.class);
        when(role.getName()).thenReturn("CodeyRole");
        when(member.getRoles()).thenReturn(List.of(role));
        assertThat(authService.hasCodeyRole(member)).isTrue();
    }


    @Test
    void is_not_elevated_member_if_user_id_not_in_elevatedUsersState() {
        var member = Mockito.mock(Member.class);
        when(member.getId()).thenReturn("not elevated");

        assertThat(authService.isElevatedMember(member)).isFalse();
    }


    @Test
    void is_elevated_member_if_user_id_in_elevated_users_state() {
        var elevatedMember = Mockito.mock(Member.class);
        when(elevatedMember.getId()).thenReturn("userId");
        assertThat(authService.isElevatedMember(elevatedMember)).isTrue();

        var normalUser = Mockito.mock(Member.class);
        when(normalUser.getId()).thenReturn("bob");
        assertThat(authService.isElevatedMember(normalUser)).isFalse();
    }
}

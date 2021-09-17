package io.horrorshow.codey.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import java.util.Objects;


@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "github_channel",
        indexes = {@Index(name = "channel_id_index", columnList = "channel_id")})
public class ChannelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "channel_id", nullable = false)
    private String channelId;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        ChannelEntity that = (ChannelEntity) o;
        return Objects.equals(id, that.id);
    }


    @Override
    public int hashCode() {
        return id.hashCode();
    }
}

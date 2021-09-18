package io.horrorshow.codey.data.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import java.util.Objects;


@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class ElevatedUser {

    @Id
    @Column(name = "user_id")
    private String userId;
    @Column(name = "user_name")
    private String name;


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        ElevatedUser that = (ElevatedUser) o;
        return Objects.equals(userId, that.userId);
    }


    @Override
    public int hashCode() {
        return userId.hashCode();
    }
}

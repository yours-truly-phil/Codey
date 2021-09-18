package io.horrorshow.codey.data.repository;

import io.horrorshow.codey.data.entity.ChannelEntity;
import org.springframework.data.repository.CrudRepository;


public interface GithubChannelRepository extends CrudRepository<ChannelEntity, Long> {

}

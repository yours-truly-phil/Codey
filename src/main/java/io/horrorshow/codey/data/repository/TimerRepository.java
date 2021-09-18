package io.horrorshow.codey.data.repository;

import io.horrorshow.codey.data.entity.TimerData;
import org.springframework.data.repository.CrudRepository;


public interface TimerRepository extends CrudRepository<TimerData, Long> {

}

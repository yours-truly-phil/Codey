package io.horrorshow.codey.data;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface TimerRepository extends CrudRepository<TimerData, Long> {

    @NotNull List<TimerData> findAll();
}

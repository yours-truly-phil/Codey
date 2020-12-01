package io.horrorshow.codey.challenge;

import io.horrorshow.codey.challenge.xml.Problem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Repository
public class ChallengeRepository {

    private final JAXBContext jaxbProblemContext
            = JAXBContext.newInstance(Problem.class);
    private final Unmarshaller problemUnmarshaller
            = jaxbProblemContext.createUnmarshaller();

    public ChallengeRepository() throws JAXBException {
    }

    public List<Problem> findAllProblems(String directory) {
        List<Problem> problemList = new ArrayList<>();
        var path = Paths.get(directory);
        log.debug("looking for challenges in {}", path.toAbsolutePath().toString());
        try (Stream<Path> stream = Files.walk(path, 1)) {
            var files = stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::toFile)
                    .collect(Collectors.toList());
            log.debug("found {} files in {}", files.size(), path.toString());
            for (File file : files) {
                try {
                    problemList.add((Problem) problemUnmarshaller.unmarshal(file));
                } catch (JAXBException e) {
                    log.error("Problem unmarshallling file {}", file, e);
                }
            }
        } catch (IOException e) {
            log.error("error walking path {}", path, e);
        }
        return problemList;
    }
}

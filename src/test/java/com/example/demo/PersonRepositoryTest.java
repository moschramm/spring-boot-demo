package com.example.demo;

import com.example.demo.model.Person;
import com.example.demo.repo.PersonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PersonRepositoryTest {

    @Autowired
    PersonRepository repo;

    @Test
    void saveAndFind() {
        Person p = new Person("Test", "t@example.com");
        Person saved = repo.save(p);
        assertThat(saved.getId()).isNotNull();
        assertThat(repo.findById(saved.getId())).isPresent();
    }
}

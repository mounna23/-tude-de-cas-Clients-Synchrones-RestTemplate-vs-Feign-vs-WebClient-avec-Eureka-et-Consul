package com.exemple.serviceclient.repositories;

import com.exemple.serviceclient.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientRepository extends JpaRepository<Client, Long> {
}

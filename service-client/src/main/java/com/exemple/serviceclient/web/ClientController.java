package com.exemple.serviceclient.web;

import com.exemple.serviceclient.entities.Client;
import com.exemple.serviceclient.repositories.ClientRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final ClientRepository repo;

    public ClientController(ClientRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Client> findAll() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Client findById(@PathVariable Long id) {
        return repo.findById(id).orElse(null);
    }

    @PostMapping
    public Client save(@RequestBody Client c) {
        return repo.save(c);
    }
}

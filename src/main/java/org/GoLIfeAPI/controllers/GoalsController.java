package org.GoLIfeAPI.controllers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/metas")
public class GoalsController {

    @GetMapping
    public String hello() {
        return "GoLife API is up and running!";
    }

}

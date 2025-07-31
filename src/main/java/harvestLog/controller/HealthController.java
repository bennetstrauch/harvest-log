package harvestLog.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
@CrossOrigin // if you need to allow cross-origin requests (optional)
public class HealthController {

    @GetMapping
    public String health() {
        return "OK";
    }
}

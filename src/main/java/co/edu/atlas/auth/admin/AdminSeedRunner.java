package co.edu.atlas.auth.admin;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminSeedRunner implements ApplicationRunner {

    private final AdminSeedService seed;

    public AdminSeedRunner(AdminSeedService seed) {
        this.seed = seed;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed.sembrar();
    }
}

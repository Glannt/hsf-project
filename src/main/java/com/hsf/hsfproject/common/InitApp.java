package com.hsf.hsfproject.common;

import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.model.Role;
import com.hsf.hsfproject.repository.ComputerItemRepository;
import com.hsf.hsfproject.repository.PCRepository;
import com.hsf.hsfproject.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class InitApp {
    @Bean
    CommandLineRunner initRoles(RoleRepository roleRepository) {
        return args -> {
            String[] roles = {"ROLE_USER", "ROLE_ADMIN"};
            for (String roleName : roles) {
                if (roleRepository.findByName(roleName) == null) {
                    Role role = new Role();
                    role.setName(roleName);
                    roleRepository.save(role);
                }
            }
        };
    }
    CommandLineRunner initComputerItems(ComputerItemRepository computerItemRepository) {
        return args -> {
            if (computerItemRepository.count() == 0) {
                ComputerItem item1 = ComputerItem.builder()
                        .name("RAM 16GB")
                        .price(120.0)
                        .build();
                ComputerItem item2 = ComputerItem.builder()
                        .name("SSD 512GB")
                        .price(90.0)
                        .build();
                ComputerItem item3 = ComputerItem.builder()
                        .name("CPU Intel i7")
                        .price(300.0)
                        .build();
                ComputerItem item4 = ComputerItem.builder()
                        .name("Mainboard B660")
                        .price(150.0)
                        .build();
                ComputerItem item5 = ComputerItem.builder()
                        .name("GPU RTX 3060")
                        .price(400.0)
                        .build();
                ComputerItem item6 = ComputerItem.builder()
                        .name("Power Supply 650W")
                        .price(80.0)
                        .build();
                ComputerItem item7 = ComputerItem.builder()
                        .name("Case ATX")
                        .price(60.0)
                        .build();
                computerItemRepository.save(item1);
                computerItemRepository.save(item2);
                computerItemRepository.save(item3);
                computerItemRepository.save(item4);
                computerItemRepository.save(item5);
                computerItemRepository.save(item6);
                computerItemRepository.save(item7);
            }
        };
    }

    @Bean
    CommandLineRunner initPCs(PCRepository pcRepository) {
        return args -> {
            if (pcRepository.count() == 0) {
                PC pc1 = PC.builder()
                        .name("Gaming PC")
                        .totalPrice(1500.0)
                        .build();
                PC pc2 = PC.builder()
                        .name("Office PC")
                        .totalPrice(800.0)
                        .build();
                pcRepository.save(pc1);
                pcRepository.save(pc2);
            }
        };
    }
}

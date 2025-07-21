package com.hsf.hsfproject.common;

import com.hsf.hsfproject.model.Category;
import com.hsf.hsfproject.model.ComputerItem;
import com.hsf.hsfproject.model.PC;
import com.hsf.hsfproject.model.Role;
import com.hsf.hsfproject.repository.CategoryRepository;
import com.hsf.hsfproject.repository.ComputerItemRepository;
import com.hsf.hsfproject.repository.PCRepository;
import com.hsf.hsfproject.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.hsf.hsfproject.model.InstalllmentType;
import com.hsf.hsfproject.repository.InstalllmentTypeRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class InitApp {
    private final CategoryRepository categoryRepository;
    private final PCRepository pcRepository;
    private final RoleRepository roleRepository;
    private final ComputerItemRepository computerItemRepository;
    private final InstalllmentTypeRepository installmentTypeRepository;
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
    @Bean
    CommandLineRunner initComputerItems(ComputerItemRepository computerItemRepository) {
        return args -> {
            if(categoryRepository.count() != 0) {
                if (computerItemRepository.count() == 0) {
                    List<ComputerItem> items = List.of(
                            ComputerItem.builder()
                                    .name("RAM 16GB")
                                    .price(120.0)
                                    .description("DDR4 3200MHz suitable for gaming and multitasking")
                                    .brand("Corsair")
                                    .model("Vengeance LPX")
                                    .category(categoryRepository.findByName("RAM"))
                                    .build(),

                            ComputerItem.builder()
                                    .name("SSD 512GB")
                                    .price(90.0)
                                    .description("Fast NVMe SSD for quick system boot and application load")
                                    .brand("Samsung")
                                    .model("980 PRO")
                                    .category(categoryRepository.findByName("Storage"))
                                    .build(),

                            ComputerItem.builder()
                                    .name("CPU Intel i7")
                                    .price(300.0)
                                    .description("12th Gen Intel Core i7 processor for high performance tasks")
                                    .brand("Intel")
                                    .model("i7-12700K")
                                    .category(categoryRepository.findByName("CPU"))
                                    .build(),

                            ComputerItem.builder()
                                    .name("Mainboard B660")
                                    .price(150.0)
                                    .description("Motherboard compatible with Intel 12th Gen CPUs")
                                    .brand("ASUS")
                                    .model("TUF Gaming B660")
                                    .category(categoryRepository.findByName("Motherboard"))
                                    .build(),

                            ComputerItem.builder()
                                    .name("GPU RTX 3060")
                                    .price(400.0)
                                    .description("Great for 1080p and 1440p gaming with Ray Tracing support")
                                    .brand("NVIDIA")
                                    .model("GeForce RTX 3060")
                                    .category(categoryRepository.findByName("GPU"))
                                    .build(),

                            ComputerItem.builder()
                                    .name("Power Supply 650W")
                                    .price(80.0)
                                    .description("80+ Bronze Certified, enough power for mid-range builds")
                                    .brand("Cooler Master")
                                    .model("MWE 650W")
                                    .category(categoryRepository.findByName("PSU"))
                                    .build(),

                            ComputerItem.builder()
                                    .name("Case ATX")
                                    .price(60.0)
                                    .description("Spacious ATX tower case with good airflow")
                                    .brand("NZXT")
                                    .model("H510")
                                    .category(categoryRepository.findByName("Case"))
                                    .build()
                    );

                    computerItemRepository.saveAll(items);
                }
            }
        };
    }

    @Bean
    CommandLineRunner initCategories(CategoryRepository categoryRepository) {
        return args -> {
            if (categoryRepository.count() == 0) {
                Category cpu = Category.builder().name("CPU").build();
                Category gpu = Category.builder().name("GPU").build();
                Category ram = Category.builder().name("RAM").build();
                Category motherboard = Category.builder().name("Motherboard").build();
                Category psu = Category.builder().name("PSU").build();
                Category caseCategory = Category.builder().name("Case").build();
                Category storage = Category.builder().name("Storage").build();
                categoryRepository.save(cpu);
                categoryRepository.save(gpu);
                categoryRepository.save(ram);
                categoryRepository.save(storage);
                categoryRepository.save(motherboard);
                categoryRepository.save(psu);
                categoryRepository.save(caseCategory);
            }
        };
    }

    @Bean
    CommandLineRunner initInstallmentTypes(InstalllmentTypeRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                List<InstalllmentType> types = List.of(
                        InstalllmentType.builder().name("3 months").months(3).interestRate(0.03).build(),
                        InstalllmentType.builder().name("6 months").months(6).interestRate(0.05).build(),
                        InstalllmentType.builder().name("12 months").months(12).interestRate(0.07).build(),
                        InstalllmentType.builder().name("24 months").months(24).interestRate(0.1).build()
                );
                repo.saveAll(types);
            }
        };
    }
//    @Bean
//    CommandLineRunner initPCs(PCRepository pcRepository) {
//        return args -> {
//            if (pcRepository.count() == 0) {
//                PC pc1 = PC.builder()
//                        .name("Gaming PC")
//                        .computerItems(1500.0)
//                        .build();
//                PC pc2 = PC.builder()
//                        .name("Office PC")
//                        .totalPrice(800.0)
//                        .build();
//                pcRepository.save(pc1);
//                pcRepository.save(pc2);
//            }
//        };
//    }
}

package com.hsf.hsfproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelpController {
    @GetMapping("/help/installments")
    public String installmentHelp() {
        return "help/installments";
    }
}
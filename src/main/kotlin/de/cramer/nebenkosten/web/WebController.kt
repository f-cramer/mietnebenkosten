package de.cramer.nebenkosten.web

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class WebController {

    @GetMapping("", "index.htm", "index.html")
    @Suppress("FunctionOnlyReturningConstant")
    fun getIndex(): String {
        return "redirect:/flats"
    }
}

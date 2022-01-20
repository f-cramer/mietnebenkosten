package de.cramer.nebenkosten.web

import de.cramer.nebenkosten.extensions.set
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("login")
class LoginController {

    @GetMapping
    fun login(
        @RequestParam error: String?,
        model: Model,
    ): String {
        model["error"] = error
        return "login"
    }
}

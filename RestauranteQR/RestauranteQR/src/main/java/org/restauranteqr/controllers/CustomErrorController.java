package org.restauranteqr.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @GetMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Integer statusCode = (Integer) request.getAttribute("jakarta.servlet.error.status_code");

        model.addAttribute("codigo", statusCode);

        if (statusCode == 404) {
            return "error/404";
        } else if (statusCode == 403) {
            return "error/403";
        } else if (statusCode == 500) {
            return "error/500";
        } else if(statusCode == 400) {
        	return "error/400";
        }

        return "error/error";
    }
    
    @GetMapping("/probar403")
    public void error403(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @SuppressWarnings("unused")
	@GetMapping("/probar500")
    public String error500() {
        int x = 10 / 0;
        return "home";
    }

}

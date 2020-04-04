package com.adrianb.scriptum.document_manager.web.controllers

import com.adrianb.scriptum.document_manager.service.GoogleDriveService
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/google")
class GoogleDriveController(
    private val googleDriveService: GoogleDriveService
) {
    private val logger = LoggerFactory.getLogger(GoogleDriveController::class.java)

    @GetMapping("/sign-in")
    fun signIn(response: HttpServletResponse) {
        logger.debug("SSO called")
        response.sendRedirect(googleDriveService.authenticate())
    }

    @GetMapping("/oauth/callback")
    fun saveAuthorisationCode(request: HttpServletRequest) {
        val code = request.getParameter("code")
        googleDriveService.exchangeCodeForTokens(code)
    }
}

package com.bonefracture.api;

import com.bonefracture.api.dto.AppointmentDtos.AppointmentResponse;
import com.bonefracture.api.dto.AppointmentDtos.CreateAppointmentRequest;
import com.bonefracture.booking.AppointmentBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Appointments of the logged-in user (the user id comes from the JWT).
 * POST /api/appointments, GET /api/appointments, GET /api/appointments/{id},
 * POST /api/appointments/{id}/confirm | cancel | reminder
 */
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentBookingService bookingService;

    public AppointmentController(AppointmentBookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AppointmentResponse request(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateAppointmentRequest request) {
        return AppointmentResponse.from(bookingService.request(userId(jwt), request.doctorId(), request.dateTime()));
    }

    @GetMapping
    public List<AppointmentResponse> myAppointments(@AuthenticationPrincipal Jwt jwt) {
        return bookingService.findForUser(userId(jwt)).stream().map(AppointmentResponse::from).toList();
    }

    @GetMapping("/{id}")
    public AppointmentResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return AppointmentResponse.from(bookingService.get(id, userId(jwt)));
    }

    @PostMapping("/{id}/confirm")
    public AppointmentResponse confirm(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return AppointmentResponse.from(bookingService.confirm(id, userId(jwt)));
    }

    @PostMapping("/{id}/cancel")
    public AppointmentResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return AppointmentResponse.from(bookingService.cancel(id, userId(jwt)));
    }

    @PostMapping("/{id}/reminder")
    public AppointmentResponse sendReminder(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        return AppointmentResponse.from(bookingService.sendReminder(id, userId(jwt)));
    }

    private Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}

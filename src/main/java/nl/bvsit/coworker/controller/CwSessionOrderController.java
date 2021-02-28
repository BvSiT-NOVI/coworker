package nl.bvsit.coworker.controller;

import nl.bvsit.coworker.payload.CwSessionOrderDTO;
import nl.bvsit.coworker.service.CwSessionOrderService;
import nl.bvsit.coworker.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/orders")
@RestController
public class CwSessionOrderController {

    @Autowired
    CwSessionOrderService cwSessionOrderService;

    @PutMapping("/{id}/served")
    public ResponseEntity<CwSessionOrderDTO> setServedCwSessionOrder(@PathVariable Long id,@AuthenticationPrincipal UserDetailsImpl authUser){
        return ResponseEntity.ok(cwSessionOrderService.setServedCwSessionOrderAuthenticated(id,authUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CwSessionOrderDTO> updateCwSessionOrder(@PathVariable Long id,
                                                                  @RequestBody CwSessionOrderDTO orderDTO,
                                                                  @AuthenticationPrincipal UserDetailsImpl authUser)
    {
        //Update is only possible if order is not yet served.
        //All CpMenuItems in the CwSessionOrder are replaced.
        return ResponseEntity.ok(cwSessionOrderService.updateOrderAuthenticated(id,orderDTO,authUser));
    }

}

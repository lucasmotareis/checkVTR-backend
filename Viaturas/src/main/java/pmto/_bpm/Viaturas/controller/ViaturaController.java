package pmto._bpm.Viaturas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pmto._bpm.Viaturas.model.Viatura;
import pmto._bpm.Viaturas.service.ViaturaService;

import java.util.List;

@RestController
@RequestMapping("viatura")
public class ViaturaController {

    private final ViaturaService viaturaService;

    public ViaturaController(ViaturaService viaturaService) {
        this.viaturaService = viaturaService;
    }

    @GetMapping
    public List<Viatura> getAllViaturas() {
        return viaturaService.getAll();

    }

    @PostMapping
    public Viatura createViatura(@RequestBody Viatura viatura){
        return viaturaService.save(viatura);
    }

    @DeleteMapping
    public void deleteViaturaById(@RequestBody Long id_viatura){
        viaturaService.delete(id_viatura);
    }





}

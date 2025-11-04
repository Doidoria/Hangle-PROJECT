import com.example.demo.domain.entity.Competition;
import com.example.demo.service.CompetitionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/competitions")
@CrossOrigin(origins = "http://localhost:3000") // React 포트 허용
public class CompetitionController {
    private final CompetitionService competitionService;

    public CompetitionController(CompetitionService competitionService) {
        this.competitionService = competitionService;
    }

    @PostMapping
    public Competition create(@RequestBody Competition competition) {
        return competitionService.create(competition);
    }

    @GetMapping
    public List<Competition> list() {
        return competitionService.findAll();
    }

    @GetMapping("/{id}")
    public Competition detail(@PathVariable Long id) {
        return competitionService.findById(id);
    }
}

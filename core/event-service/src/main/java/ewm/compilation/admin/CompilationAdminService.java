package ewm.compilation.admin;


import ru.practicum.dto.CompilationDto;
import ru.practicum.dto.UpdateCompilationRequest;
import ru.practicum.dto.NewCompilationDto;

public interface CompilationAdminService {

    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    CompilationDto updateCompilation(UpdateCompilationRequest updateCompilationRequest, Long compId);

    void deleteCompilation(Long compId);
}

package ewm.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ewm.category.model.Category;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    Category toEntity(NewCategoryDto newCategoryDto);

    CategoryDto toDto(Category category);
}
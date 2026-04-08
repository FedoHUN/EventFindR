package sk.eventfindr.fsa.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.eventfindr.fsa.domain.User;
import sk.eventfindr.fsa.domain.UserRole;
import sk.eventfindr.fsa.rest.dto.CreateUserRequestDto;
import sk.eventfindr.fsa.rest.dto.UserDto;
import sk.eventfindr.fsa.rest.dto.UserRoleDto;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(CreateUserRequestDto request);

    default UserRoleDto map(UserRole value) {
        return value == null ? null : UserRoleDto.fromValue(value.name());
    }

    default UserRole map(UserRoleDto value) {
        return value == null ? null : UserRole.valueOf(value.name());
    }
}

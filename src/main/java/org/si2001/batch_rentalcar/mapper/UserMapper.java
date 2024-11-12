package org.si2001.batch_rentalcar.mapper;


import org.example.entities.User;
import org.mapstruct.Mapper;
import org.si2001.batch_rentalcar.dto.UserDTO;


@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDTO(User user);

    User toEntity(UserDTO userDTO);
}

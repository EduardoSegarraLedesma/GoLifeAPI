package org.GoLIfeAPI.mapper.service;

import org.GoLIfeAPI.dto.goal.ResponsePartialGoalDTO;
import org.GoLIfeAPI.dto.user.CreateUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserDTO;
import org.GoLIfeAPI.dto.user.ResponseUserStatsDTO;
import org.GoLIfeAPI.model.user.User;
import org.GoLIfeAPI.model.user.UserStats;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserDtoMapper {

    private final GoalDtoMapper goalDtoMapper;

    public UserDtoMapper(GoalDtoMapper goalDtoMapper) {
        this.goalDtoMapper = goalDtoMapper;
    }

    // Map Input DTOs to POJOs

    public User mapCreateUserDtoToUser(CreateUserDTO newUserDto, String uid) {
        return new User(uid, newUserDto.getApellidos(), newUserDto.getNombre());
    }

    // Map POJOs to Output DTOs

    public ResponseUserDTO mapUserToResponseUserDTO(User user) {
        List<ResponsePartialGoalDTO> partialGoalDTOs = Collections.emptyList();
        if (user.getMetas() != null) {
            partialGoalDTOs = user.getMetas().stream()
                    .map(Goal -> goalDtoMapper.mapPartialGoalToResponsePartialGoalDTO(Goal))
                    .collect(Collectors.toList());
        }
        return new ResponseUserDTO(
                user.getNombre(),
                user.getApellidos(),
                partialGoalDTOs,
                mapUserStatsToResponseUserStatsDTO(user.getEstadisticas()));
    }

    public ResponseUserStatsDTO mapUserStatsToResponseUserStatsDTO(UserStats userStats) {
        int totalMetas = userStats.getTotalMetas();
        int totalMetasFinalizadas = userStats.getTotalMetasFinalizadas();
        return new ResponseUserStatsDTO(
                totalMetas,
                totalMetasFinalizadas,
                calculatePorcentajeFinalizadas(totalMetas, totalMetasFinalizadas));
    }

    private BigDecimal calculatePorcentajeFinalizadas(int totalMetas, int totalMetasFinalizadas) {
        if (totalMetas > 0 && totalMetasFinalizadas > 0) {
            return BigDecimal.valueOf(totalMetasFinalizadas)
                    .divide(BigDecimal.valueOf(totalMetas), 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
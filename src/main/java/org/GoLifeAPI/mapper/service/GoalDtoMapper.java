package org.GoLifeAPI.mapper.service;

import org.GoLifeAPI.dto.goal.*;
import org.GoLifeAPI.dto.record.ResponseBoolRecordDTO;
import org.GoLifeAPI.dto.record.ResponseNumRecordDTO;
import org.GoLifeAPI.model.goal.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class GoalDtoMapper {

    DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
    private final RecordDtoMapper recordDtoMapper;


    public GoalDtoMapper(RecordDtoMapper recordDtoMapper) {
        this.recordDtoMapper = recordDtoMapper;
    }

    // Map Input DTOs to POJOs

    public BoolGoal mapCreateBoolGoalDtoToBoolGoal(CreateBoolGoalDTO newGoalDto, String uid, LocalDate fechaFin) {
        return new BoolGoal(
                uid, newGoalDto.getNombre(), newGoalDto.getDescripcion(), newGoalDto.getFecha(), false,
                newGoalDto.getDuracionValor(), newGoalDto.getDuracionUnidad(), false, fechaFin);
    }

    public NumGoal mapCreateNumGoalDtoToNumGoal(CreateNumGoalDTO newGoalDto, String uid, LocalDate fechaFin) {
        return new NumGoal(
                uid, newGoalDto.getNombre(), newGoalDto.getDescripcion(), newGoalDto.getFecha(), false,
                newGoalDto.getDuracionValor(), newGoalDto.getDuracionUnidad(), false, fechaFin,
                newGoalDto.getValorObjetivo(), newGoalDto.getUnidad());
    }

    // Map POJOs to Output DTOs

    public ResponseBoolGoalDTO mapBoolGoalToResponseBoolGoalDTO(BoolGoal newBoolGoal, BoolGoal oldBoolGoal) {
        List<ResponseBoolRecordDTO> registros = Collections.emptyList();
        if (newBoolGoal.getRegistros() != null) {
            registros = newBoolGoal.getRegistros().stream()
                    .map(record -> recordDtoMapper.mapBoolRecordToResponseBoolRecordDto(record))
                    .collect(Collectors.toList());
        }
        return new ResponseBoolGoalDTO(
                newBoolGoal.get_id().toString(),
                newBoolGoal.getNombre(),
                newBoolGoal.getTipo(),
                newBoolGoal.getDescripcion(),
                newBoolGoal.getFecha().format(formatter),
                newBoolGoal.getFinalizado(),
                newBoolGoal.getDuracionValor(),
                newBoolGoal.getDuracionUnidad(),
                mapGoalStatsToResponseGoalStatsDTO(newBoolGoal.getEstadisticas(), oldBoolGoal.getEstadisticas()),
                registros
        );
    }

    public ResponseNumGoalDTO mapNumGoalToResponseNumGoalDTO(NumGoal newNumGoal, NumGoal oldNumGoal) {
        List<ResponseNumRecordDTO> registros = Collections.emptyList();
        if (newNumGoal.getRegistros() != null) {
            registros = newNumGoal.getRegistros().stream()
                    .map(record -> recordDtoMapper.mapNumRecordToResponseNumRecordDto(record))
                    .collect(Collectors.toList());
        }
        return new ResponseNumGoalDTO(
                newNumGoal.get_id().toString(),
                newNumGoal.getNombre(),
                newNumGoal.getTipo(),
                newNumGoal.getDescripcion(),
                newNumGoal.getFecha().format(formatter),
                newNumGoal.getFinalizado(),
                newNumGoal.getDuracionValor(),
                newNumGoal.getDuracionUnidad(),
                mapGoalStatsToResponseGoalStatsDTO(newNumGoal.getEstadisticas(), oldNumGoal.getEstadisticas()),
                registros,
                newNumGoal.getValorObjetivo(),
                newNumGoal.getUnidad()
        );
    }

    private ResponseGoalStatsDTO mapGoalStatsToResponseGoalStatsDTO(GoalStats newGoalStats, GoalStats oldGoalStats) {
        return new ResponseGoalStatsDTO(
                newGoalStats.getValorAlcanzado(),
                (newGoalStats.getValorAlcanzado() && !oldGoalStats.getValorAlcanzado()),
                newGoalStats.getFechaFin() != null ? newGoalStats.getFechaFin().format(formatter) : "");
    }

    public ResponsePartialGoalDTO mapPartialGoalToResponsePartialGoalDTO(PartialGoal partialGoal) {
        if (partialGoal instanceof PartialNumGoal partialNumGoal)
            return mapPartialGoalToResponsePartialNumGoalDTO(partialNumGoal);
        else
            return mapPartialGoalToResponsePartialBoolGoalDTO(partialGoal);
    }

    public ResponsePartialGoalDTO mapPartialGoalToResponsePartialBoolGoalDTO(PartialGoal partialGoal) {
        return new ResponsePartialGoalDTO(
                partialGoal.get_id(),
                partialGoal.getNombre(),
                partialGoal.getTipo(),
                partialGoal.getFecha().format(formatter),
                partialGoal.getFinalizado(),
                partialGoal.getDuracionValor(),
                partialGoal.getDuracionUnidad()
        );
    }

    public ResponsePartialNumGoalDTO mapPartialGoalToResponsePartialNumGoalDTO(PartialNumGoal partialGoal) {
        return new ResponsePartialNumGoalDTO(
                partialGoal.get_id(),
                partialGoal.getNombre(),
                partialGoal.getTipo(),
                partialGoal.getFecha().format(formatter),
                partialGoal.getFinalizado(),
                partialGoal.getDuracionValor(),
                partialGoal.getDuracionUnidad(),
                partialGoal.getValorObjetivo(),
                partialGoal.getUnidad()
        );
    }
}